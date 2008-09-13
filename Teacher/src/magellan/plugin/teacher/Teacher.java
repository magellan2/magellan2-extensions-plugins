// class magellan.plugin.ECTester
// created on Dec 30, 2007
//
// Copyright 2003-2007 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.plugin.teacher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import magellan.client.swing.ProgressBarUI;
import magellan.library.ID;
import magellan.library.Named;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.utils.NullUserInterface;
import magellan.library.utils.UserInterface;
import magellan.library.utils.logging.Logger;

/**
 * Solves the teaching problem employing a simple genetic algorithm.
 * 
 * @author stm
 * 
 */
public class Teacher {
	private static final Logger log = Logger.getInstance(Teacher.class);

	public static final String TEACH_TAG = "ejcTaggableComparator2";
	public static final String LEARN_TAG = "ejcTaggableComparator";
	// private static final String TEACH_TAG = "Teaching";
	// private static final String LEARN_TAG = "Learning";

	Collection<Unit> units = Collections.emptyList();
	String namespace = null;

	public boolean confirmFullTeachers = true;

	public boolean confirmEmptyTeachers = false;

	public boolean confirmTaughtStudents = true;

	public boolean confirmUntaughtStudents = false;

	public int percentFull;

	private UserInterface ui = new NullUserInterface();

	public boolean isConfirmUntaughtStudents() {
		return confirmUntaughtStudents;
	}

	public void setConfirmUntaughtStudents(boolean confirmUntaughtStudents) {
		this.confirmUntaughtStudents = confirmUntaughtStudents;
	}

	Teacher(Collection<Unit> units, String namespace, UserInterface ui) {
		this.units = units;
		this.namespace = namespace;
		this.ui = ui;
	}

	@SuppressWarnings("serial")
	public static class OrderFormatException extends Exception {

		public OrderFormatException() {
			super();
		}

		public OrderFormatException(String message, Throwable cause) {
			super(message, cause);
		}

		public OrderFormatException(String message) {
			super(message);
		}

		public OrderFormatException(Throwable cause) {
			super(cause);
		}

	}

	private SUnit[] sUnits;
	public static final String delim = " ";

	Random random = new Random();

	private boolean stopFlag = false;

	/**
	 * Represents a unit with teaching and learning preferences.
	 * 
	 * @author stm
	 * 
	 */
	public static class SUnit implements Named {

		private Unit unit;
		private Map<String, Integer> teach = new HashMap<String, Integer>();
		private Map<String, Integer> targets = new HashMap<String, Integer>();
		private Map<String, Integer> maxs = new HashMap<String, Integer>();
		double prio = 1;

		/** calculated priorities */
		private Map<String, Double> weights = new HashMap<String, Double>();

		private Map<String, Integer> talentLevels = new HashMap<String, Integer>();

		private int index;
		private ArrayList<Integer> teachers = new ArrayList<Integer>();

		SUnit(Unit unit) {
			this.unit = unit;
			this.prio = 1;
		}

		public void addTeach(String talent, Integer diff) {
			teach.put(talent, diff);
			talentLevels.put(talent, getLevel(getUnit(), talent));
			weights.clear();
		}

		public void addLearn(String talent, Integer target, Integer max) {
			if (target == 0)
				throw new IllegalArgumentException("target must be > 0");
			targets.put(talent, target);
			maxs.put(talent, max);
			talentLevels.put(talent, getLevel(getUnit(), talent));
			weights.clear();
		}

		public Unit getUnit() {
			return unit;
		}

		public double calcWeight(String skill) {
			if (skill == null)
				return 0;
			Double prio = weights.get(skill);
			if (prio == null) {
				// calc max mult
				double maxMult = 0;
				for (String skill2 : getLearnTalents()) {
					int lev = Math.max(1, getSkillLevel(skill2));
					if (lev < getMax(skill2)) {
						double mult = lev / (double) getTarget(skill2);
						if (maxMult < mult)
							maxMult = mult;
					}
				}
				if (maxMult == 0) {
					// all skills above max
					prio = .5;
					weights.put(skill, prio);
				} else {
					// calc max normalized learning weeks
					double maxWeeks = 0;
					for (String skill2 : getLearnTalents()) {
						int lev = Math.max(1, getSkillLevel(skill2));
						if (lev < getMax(skill2)) {
							double weeks = getTarget(skill2) - lev / maxMult + 2;
							// double weeks = getWeeks(getTarget(skill2)) - getWeeks(lev/maxMult) +2;
							if (maxWeeks < weeks)
								maxWeeks = weeks;
						}
					}
					int level = Math.max(1, getSkillLevel(skill));
					if (level >= getMax(skill))
						prio = 0d;
					else
						prio = (getTarget(skill) - level / maxMult + 2) / maxWeeks;
					// prio = (getWeeks(getTarget(skill)) - getWeeks(level/maxMult) + 2)/maxWeeks;
					if (prio < 0)
						prio = prio * 1.000001;
					weights.put(skill, prio);
				}
				getUnit().addOrder("; $$$" + skill + " " + prio, false, 0);
			}
			return prio;
		}

		private double getWeeks(double d) {
			return (d) * (d + 1) / 2d;
		}

		public int getTarget(String skill) {
			if (skill == null)
				return 0;
			return (targets.get(skill) != null ? targets.get(skill) : -1);
		}

		public int getMax(String skill) {
			if (skill == null)
				return 0;
			return (maxs.get(skill) != null ? maxs.get(skill) : -1);
		}

		public double getPrio() {
			return prio;
		}

		public void setPrio(double prio) {
			this.prio = prio;
		}

		public int getMaximumDifference(String teaching) {
			if (teaching == null)
				return 1;
			return (teach.get(teaching) != null ? teach.get(teaching) : 1);
		}

		public Set<String> getLearnTalents() {
			return targets.keySet();
		}

		public Set<String> getTeachTalents() {
			return teach.keySet();
		}

		void setIndex(int index) {
			this.index = index;
		}

		public int getIndex() {
			return index;
		}

		void addTeacher(int index) {
			teachers.add(index);
		}

		List<Integer> getTeachers() {
			return teachers;
		}

		public String toString() {
			return index + ":" + getUnit().toString();
		}

		public void attachTags() {
			Unit u = getUnit();
			double maxTarget = Double.NEGATIVE_INFINITY;
			String maxLearning = null;
			String maxTeaching = null;
			for (String t : getLearnTalents()) {
				if (getTarget(t) > maxTarget) {
					maxTarget = getTarget(t);
					maxLearning = t;
				}

			}
			int maxTalent = 0;
			for (String t : getTeachTalents()) {
				if (getLevel(u, t) > maxTalent) {
					maxTalent = getLevel(u, t);
					maxTeaching = t;
				}

			}
			if (maxLearning != null) {
				u.putTag(LEARN_TAG, maxLearning);
			}
			if (maxTeaching != null) {
				u.putTag(TEACH_TAG, maxTeaching);
			}

		}

		public String getModifiedName() {
			return getUnit().getModifiedName();
		}

		public String getName() {
			return getUnit().getName();
		}

		public void setName(String name) {

		}

		public int compareTo(Object o) {
			if (o instanceof SUnit) {
				return getUnit().compareTo(((SUnit) o).getUnit());
			}
			return 0;
		}

		public ID getID() {
			return getUnit().getID();
		}

		public Object clone() throws CloneNotSupportedException {
			throw new CloneNotSupportedException();
		}

		/**
		 * More efficient variant of getLevel(SUnig, String).
		 * 
		 * @param skill
		 * @return
		 */
		public int getSkillLevel(String skill) {
			if (talentLevels != null) {
				Integer result = talentLevels.get(skill);
				if (result != null) {
					return result;
				}
			}
			int level = getLevel(getUnit(), skill);
			talentLevels.put(skill, level);
			return level;
		}

	}

	/**
	 * Represents a solution to the teaching problem.
	 * 
	 * @author steffen
	 */
	class Solution implements Comparable<Solution>, Cloneable {

		/**
		 * Represents what a unit is doing in a solution (teaching or learning etc.)
		 * 
		 * @author steffen
		 */
		class Info implements Cloneable {
			private SUnit unit = null;
			String learning = null;
			private int students = 0;
			private int teacher = -1;

			public Info(SUnit unit) {
				this.unit = unit;
				assignLearn();
			}

			public void assignLearn() {
				learning = (String) unit.getLearnTalents().toArray()[random.nextInt(unit.getLearnTalents()
						.size())];
			}

			public String toString() {
				return unit.toString() + " " + learning + " " + students + " " + teacher;
			}

			public SUnit getSUnit() {
				return unit;
			}

			public Unit getUnit() {
				return unit.getUnit();
			}

			public void clearTeachers() {
				teacher = -1;
			}

			public void addTeacher(int t) {
				teacher = t;
			}

			public void removeTeacher(int t) {
				if (teacher == t)
					teacher = -1;
			}

			public int getNumTeachers() {
				if (teacher == -1)
					return 0;
				return infos[teacher].getUnit().getModifiedPersons();
			}

			public Collection<Integer> getTeacherSet() {
				if (teacher == -1)
					return Collections.emptySet();
				else
					return Collections.singleton(teacher);
			}

			public int getTeacher() {
				return teacher;
			}

			public int getRandomTeacher() {
				List<Integer> teachers = getSUnit().getTeachers();
				if (teachers.size() == 0)
					return -1;
				return teachers.get(random.nextInt(teachers.size()));
			}

			public int getFreeTeacher() {
				List<Integer> teachers = getSUnit().getTeachers();
				if (teachers.size() == 0)
					return -1;
				int firstTeacher = random.nextInt(teachers.size());
				for (int i = 0; i < teachers.size(); ++i) {
					if (validTeacher(this, teachers.get((firstTeacher + i) % teachers.size())))
						return teachers.get((firstTeacher + i) % teachers.size());
				}

				return -1;
			}

			public Info clone() {
				try {
					return (Info) super.clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
					return null;
				}
			}

		}

		// private static final double LEVEL_VALUE = .1;
		private static final double WRONGLEVEL_VALUE = .5;

		SUnit[] units;
		Info[] infos;

		boolean changed = true;
		double result = 0;

		/**
		 * Creates a valid solution with the specified units.
		 * 
		 * @param units
		 */
		public Solution(SUnit[] units) {
			this.units = units;
			init();
		}

		void init() {
			this.infos = new Info[this.units.length];
			for (int i = 0; i < infos.length; ++i) {
				Info info = new Info(units[i]);
				infos[i] = info;
			}
			changed = true;
			result = 0;
		}

		public Solution clone() {
			Solution result = null;
			try {
				result = (Solution) super.clone();
			} catch (CloneNotSupportedException e) {
			}
			result.infos = new Info[infos.length];
			for (int i = 0; i < infos.length; i++) {
				result.infos[i] = infos[i].clone();
			}
			return result;
		}

		/**
		 * The value of a solution is roughly a sum over all learning units. Each learning unit
		 * contributes the value of the talent it is learning. This is doubled if the unit has a
		 * teacher.
		 * 
		 * @return The value of this solution
		 */
		public double evaluate() {
			if (!changed)
				return result;
			result = 0;
			for (int i = 0; i < infos.length; ++i) {
				Info info = infos[i];
				SUnit su = info.getSUnit();
				double value = 0;
				if (info.learning != null) {
					value = su.calcWeight(info.learning);
					value *= su.getUnit().getModifiedPersons();
					value *= su.getPrio();
					int sLevel = info.getSUnit().getSkillLevel(info.learning);
					value *= Math.sqrt(sLevel) / 3.5;
					if (info.getNumTeachers() > 0) {
						Info teacher = infos[info.teacher];
						int tLevel = teacher.getSUnit().getSkillLevel(info.learning);
						int maxDiff = teacher.getSUnit().getMaximumDifference(info.learning);
						if (maxDiff == 1) {
							value = 0;
						} else if (tLevel - sLevel < 2) {
							log.warn("diff<2");
							value = 0;
						} else if (maxDiff != 0 && maxDiff < tLevel - sLevel) {
							value *= (1 + WRONGLEVEL_VALUE * sLevel / (double) tLevel);
						} else
							value *= 2;
					}
				}
				result += value;
			}
			return result;
		}

		/**
		 * Returns a positive value if this solutions value is smaller than o's value.
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Solution o) {
			double e2 = ((Solution) o).evaluate();
			double e1 = evaluate();

			return (int) (e2 > e1 ? Math.ceil(e2 - e1) : Math.floor(e2 - e1));
		}

		/**
		 * Randomly mutate this solution
		 * 
		 * @param mutationProb
		 *          Influences probability of an mutation occuring. Should be between 0 and 1.
		 */
		public void mutate(double mutationProb) {
			double teachingProb = .3;
			double teacherProb = .3;

			for (int i = 0; i < infos.length; ++i) {
				Info info = infos[i];
				// SUnit su = info.getSUnit();
				if (random.nextDouble() < mutationProb) {

					// change teacher status
					if (random.nextDouble() < teachingProb) {
						if (info.learning == null) {
							// learn new talent
							info.assignLearn();
							info.clearTeachers();
							info.students = 0;
						} else {
							// become teacher
							info.learning = null;
							info.students = 0;
							if (info.getTeacher() != -1)
								infos[info.getTeacher()].students -= info.getUnit().getModifiedPersons();
							info.clearTeachers();
						}
					}

					// assign new teacher
					if (info.learning != null && random.nextDouble() < teacherProb) {
						if (info.getTeacher() != -1)
							infos[info.getTeacher()].students -= info.getUnit().getModifiedPersons();
						info.clearTeachers();
					}
				}
			}

			assignTeachers();
			changed = true;
		}
		
		/**
		 * Set this unit to learn its most valuable talent.
		 */
		public void fix(){
			for (int i = 0; i < infos.length; ++i) {
				Info info = infos[i];
				SUnit u = info.unit;
				if (info.learning==null && info.students==0){
					String maxT = null;
					double max = -1;
					for (String t : u.getLearnTalents()){
						if (u.calcWeight(t) > max){
							max=u.calcWeight(t);
							maxT = t;
						}
					}
					log.info("fixing "+u+": "+maxT);
					info.learning = maxT;
				}
			}
		}

		/**
		 * Tries to assign a teacher to every unit not already having one.
		 */
		protected void assignTeachers() {
			Collection<Integer> teacherSet = new ArrayList<Integer>();

			// reset teachers
			for (int i = 0; i < infos.length; ++i) {
				Info info = infos[i];
				if (info.learning == null) {
					teacherSet.add(new Integer(i));
				}
				if (info.getTeacher() != -1 && infos[info.getTeacher()].learning != null)
					info.clearTeachers();

			}

			if (teacherSet.size() <= 0)
				return;

			// Integer[] teachers = (Integer[]) teacherSet.toArray(new Integer[0]);

			// assign teachers
			for (int i = 0; i < infos.length; ++i) {
				Info student = infos[i];
				if (student.learning != null) {
					if (student.getTeacher() == -1) {
						// find new teacher
						// int teacher = student.getRandomTeacher();
						int teacher = student.getFreeTeacher();
						if (teacher != -1 && validTeacher(student, teacher)) {
							assignTeacher(student, teacher);
						}
						// int firstTeacher = random.nextInt(teachers.length);
						// for (int j = 0; j < teachers.length; ++j) {
						// int current = (firstTeacher + j) % teachers.length;
						// // suitable teacher?
						// if (validTeacher(student, teachers[current])) {
						// assignTeacher(student, teachers[current]);
						// break;
						// }
						// }
					}
				}
			}
			// // assign teachers, 2nd try
			// for (int i = 0; i < infos.length; ++i) {
			// Info student = infos[i];
			// if (student.learning != null) {
			// if (student.getTeacher()==-1) {
			// // find new teacher
			// int firstTeacher = random.nextInt(teachers.length);
			// for (int j = 0; j < teachers.length; ++j) {
			// int current = (firstTeacher + j) % teachers.length;
			// // suitable teacher?
			// if (validTeacher(student, teachers[current], true)) {
			// assignTeacher(student, teachers[current]);
			// break;
			// }
			// }
			// }
			// }
			// }

		}

		/**
		 * Combines to solutions resulting in a new solution similar to the two old ones.
		 * 
		 * @param solution
		 * @param solution2
		 */
		public void mate(Solution solution, Solution solution2) {
			if (solution.units != solution2.units)
				throw new IllegalArgumentException("incompatible solutions");
			init();

			byte[] parents = new byte[infos.length];
			random.nextBytes(parents);

			// first step: only assign "roles"
			for (int i = 0; i < infos.length; ++i) {
				Info info = infos[i];
				Info parent;
				if (parents[i] % 2 == 0)
					parent = solution.infos[i];
				else
					parent = solution2.infos[i];
				info.learning = parent.learning;
				info.students = 0;
				info.clearTeachers();
			}

			// assign teachers
			for (int i = 0; i < infos.length; ++i) {
				Info student = infos[i];
				Info parent;
				if (parents[i] % 2 == 0)
					parent = solution.infos[i];
				else
					parent = solution2.infos[i];
				if (student.learning != null && parent.getTeacher() != -1) {
					if (validTeacher(student, parent.getTeacher()))
						assignTeacher(student, parent.getTeacher());
				}
			}
			assignTeachers();
			changed = true;
		}

		private boolean validTeacher(Info student, int t) {
			return validTeacher(student, t, false);
		}

		private boolean validTeacher(Info student, int t, boolean partial) {
			Info teacher = infos[t];
			if (teacher.learning != null
					|| teacher.getSUnit().getSkillLevel(student.learning) < student.getSUnit().getSkillLevel(
							student.learning) + 2)
				return false;
			else
				return partial
						|| teacher.getUnit().getModifiedPersons() * 10 >= teacher.students
								+ student.getUnit().getModifiedPersons();

		}

		public void assignTeacher(Info student, int t) {
			Info teacher = infos[t];
			student.addTeacher(t);
			teacher.students += student.getUnit().getModifiedPersons();
		}
	}

	/**
	 * Returns the skill level of a unit. For example getLevel(unit,"Unterhaltung")
	 */
	public static int getLevel(Unit unit, String skillName) {
		Collection<Skill> skills = unit.getModifiedSkills();
		if (skills != null) {
			for (Skill skill : skills) {
				if (skill.getSkillType().getName().equalsIgnoreCase(skillName)) {
					return skill.getLevel();
				}
			}
		}
		return 0;
	}

	/**
	 * Extracts the teaching units from the orders.
	 * 
	 * @param namespace
	 *          Extract only units of this namespace.
	 * 
	 * @return A List of units who are teachers or students
	 */
	public Collection<SUnit> getUnits(String namespace, boolean setTags) {

		Collection<SUnit> result = new ArrayList<SUnit>(units.size());
		for (Unit u : units) {
			SUnit su = parseUnit(u, namespace, setTags);
			if (su != null) {
				result.add(su);
				su.setIndex(result.size() - 1);
			}
		}
		for (SUnit student : result) {
			for (SUnit teacher : result) {
				if (valid(student, teacher))
					student.addTeacher(teacher.getIndex());
			}
		}
		return result;
	}

	/**
	 * @param student
	 * @param teacher
	 * @return <code>true</code> iff teacher qualifies as a teacher (in some skill) for student
	 */
	private boolean valid(SUnit student, SUnit teacher) {
		boolean result = false;
		for (String talent : student.getLearnTalents()) {
			int diff = teacher.getMaximumDifference(talent);

			if (student.getTarget(talent) > 0 && diff != 1
			// && getLevel(teacher.getUnit(), talent) - getLevel(student.getUnit(), talent)
					// <=diff
					&& getLevel(teacher.getUnit(), talent) - getLevel(student.getUnit(), talent) >= 2) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Parse the orders of <code>u</code>.
	 * 
	 * @param u
	 * @return A {@link SUnit} according to <code>u</code>'s orders, <code>null</code> if this unit
	 *         has no teaching or learning orders
	 */
	public static SUnit parseUnit(Unit u, String namespace, boolean setTags) {
		SUnit su = null;
		boolean errorFlag = false;
		for (String orderLine : u.getOrders()) {
			try {
				OrderList orderList;
				try {
					orderList = parseOrder(u, orderLine, getTeachTag(namespace), getLearnTag(namespace));
				} catch (OrderFormatException e) {
					orderList = new OrderList(Order.LEARN);
				}
				for (Order order : orderList.orders) {
					String talent = order.getTalent();
					if (order.getType() == Order.TEACH) {
						int diff = order.getDiff();
						if (su == null)
							su = new SUnit(u);
						if (talent.equals(Order.ALL)) {
							if (u.getModifiedSkills().isEmpty())
								continue;

							// add all skills weighted by their level
							for (Skill s : u.getModifiedSkills()) {
								su.addTeach(s.getName(), diff);
							}
						} else {
							su.addTeach(talent, diff);
						}
					} else {
						int target = order.getTarget();
						int max = order.getMax();
						if (su == null)
							su = new SUnit(u);
						if (talent.equals(Order.ALL)) {
							throw new IllegalArgumentException("L ALL not supported");
						} else {
							su.addLearn(talent, target, max);
							su.setPrio(orderList.getPrio());
						}
					}
				}
			} catch (Exception e) {
				// log.warn(e + " parse error, unit " + u + " line " + orderString);
				errorFlag = true;
				su = null;
			}
		}

		if (errorFlag || (su != null && su.getLearnTalents().isEmpty())) {
			u.setOrdersConfirmed(false);
			if (setTags)
				u.putTag(LEARN_TAG, "error");
			if (errorFlag) {
				u.addOrder("; $$$ teach error: syntax error", false, 0);
			} else {
				u.addOrder("; $$$ teach error: unit needs L order", false, 0);
			}
			return null;
		}

		if (setTags && su != null)
			su.attachTags();
		return su;
	}

	/**
	 * Tries to parse orderLine.
	 * 
	 * @param orderLine
	 * @param teachTag
	 * @param learnTag
	 * @return
	 * @throws OrderFormatException
	 */
	protected static OrderList parseOrder(Unit unit, String orderLine, String teachTag,
			String learnTag) throws OrderFormatException {
		OrderList result = new OrderList(Order.LEARN);
		try {
			// try to find out which kind of order we have
			int start = orderLine.indexOf(teachTag);
			if (start != -1) {
				// teach order
				result = new OrderList(Order.TEACH);
				StringTokenizer st = new StringTokenizer(orderLine.substring(start + learnTag.length()),
						delim, false);

				while (st.hasMoreElements()) {
					String talent = st.nextToken();
					int diff = Integer.parseInt(st.nextToken());
					result.addOrder(new Order(talent, diff, true));
				}
			} else {
				start = orderLine.indexOf(learnTag);
				if (start != -1) {
					// learn order
					result = new OrderList(Order.LEARN);
					StringTokenizer st = new StringTokenizer(orderLine.substring(start + teachTag.length()),
							delim, false);
					// try to read priority
					if (!st.hasMoreElements()) {
						return result;
					}
					String first = st.nextToken();
					double prio = -1;
					try {
						prio = Double.parseDouble(first);
					} catch (NumberFormatException e) {
					}
					if (prio > 0) {
						result.setPrio(prio);
						// new format
						while (st.hasMoreElements()) {
							String talent = st.nextToken();
							int target = Integer.parseInt(st.nextToken());
							int max = Integer.parseInt(st.nextToken());
							result.addOrder(new Order(talent, prio, target, max));
						}
					} else {
						// no priority ==> old format
						while (st.hasMoreElements()) {
							String talent = first;
							if (talent == null)
								talent = st.nextToken();
							first = null;
							double talentPrio = Double.parseDouble(st.nextToken());
							if (Order.ALL.equals(talent)) {
								double prio2 = Double.parseDouble(st.nextToken());
								convertOrder(unit, talent, talentPrio, prio2, result);
							} else {
								convertOrder(unit, talent, talentPrio, result);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw new OrderFormatException("parse error in line " + orderLine, e);
		}
		return result;
	}

	public static class OrderList {
		public List<Order> orders = new ArrayList<Order>(1);
		String type = null;

		private double prio = 1;

		public OrderList(String type) {
			this.type = type;
		}

		public void addOrder(Order o) {
			orders.add(o);
			if (type.equals(Order.LEARN))
				setPrio(Math.max(getPrio(), o.getPrio()));
		}

		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		public void setPrio(double prio) {
			this.prio = prio;

		}

		public String toString() {
			return getName("");
		}

		/**
		 * @return the prio
		 */
		public double getPrio() {
			return prio;
		}

		public String getName(String namespace) {
			StringBuffer sb = new StringBuffer();
			sb.append("// ");
			if (type.equals(Order.TEACH))
				sb.append(getTeachTag(namespace));
			else {
				sb.append(getLearnTag(namespace));
				sb.append(" ");
				sb.append(getPrio());
			}
			for (Order o : orders) {
				sb.append(" ");
				sb.append(o.shortOrder());
			}
			return sb.toString();
		}
	}

	/**
	 * Tries to emulate an old style line <code>// $$L Hiebwaffen 100.0</code> into a new one
	 * <code>// $$L 100.0 Hiebwaffen 10 999</code>·
	 * 
	 * @param unit
	 * @param talent
	 * @param talentPrio
	 * @param result
	 */
	private static void convertOrder(Unit unit, String talent, double talentPrio, OrderList result) {
		if (result.getPrio() < talentPrio)
			result.setPrio(talentPrio);

		result.addOrder(new Order(talent, talentPrio, Math.max(1, getLevel(unit, talent)), 999));
	}

	/**
	 * Tries to emulate an old style line <code>// $$L ALLES 100.0 50.0</code> into a new one
	 * <code>// $$L 100.0 Hiebwaffen 10 999 Ausdauer 6 999</code>·
	 * 
	 * @param unit
	 * @param talent
	 * @param talentPrio
	 * @param result
	 */
	private static void convertOrder(Unit unit, String talent, double talentPrio, double prio2,
			OrderList result) {
		if (unit.getModifiedSkills().isEmpty())
			return;

		// find min an max skill
		int maxSkill = 1, minSkill = Integer.MAX_VALUE;
		for (Skill s : unit.getModifiedSkills()) {
			int l = s.getLevel();
			if (l > maxSkill)
				maxSkill = l;
			if (l < minSkill)
				minSkill = l;
		}
		// add all skills weighted by their level
		for (Skill s : unit.getModifiedSkills()) {
			result.addOrder(new Order(s.getName(), talentPrio, Math.max(1, s.getLevel()), 999));
		}
	}

	public static void convert(Collection<Unit> values, String namespace) {
		for (Unit u : values) {
			Collection<String> newOrders = new ArrayList<String>(u.getOrders().size());
			boolean changed = false;
			for (String orderLine : u.getOrders()) {
				OrderList orderList;
				try {
					orderList = parseOrder(u, orderLine, getTeachTag(namespace), getLearnTag(namespace));
				} catch (OrderFormatException e) {
					orderList = new OrderList(Order.TEACH);
				}
				if (orderList.orders.isEmpty()) {
					newOrders.add(orderLine);
				} else {
					changed = true;
					StringBuffer sb = new StringBuffer();
					newOrders.add(orderList.getName(namespace));
				}
			}
			if (changed) {
				u.setOrders(newOrders);
			}
		}
	}

	/**
	 * Parses all units and sets tags.
	 * 
	 */
	public void parse() {
		for (Unit u : units) {
			parseUnit(u, namespace, true);
		}
	}

	/**
	 * Parses all units and sets tags.
	 * 
	 */
	public void unTag() {
		for (Unit u : units) {
			u.removeTag(TEACH_TAG);
			u.removeTag(LEARN_TAG);
		}
	}

	/**
	 * Parses all units and runs the optimization algorithm.
	 * 
	 * @return The value of the best solution
	 */
	public double mainrun() {
		sUnits = (SUnit[]) getUnits(namespace, false).toArray(new SUnit[0]);
		log.info("teaching " + sUnits.length + " units in namespace \"" + namespace + "\"");

		if (sUnits.length == 0)
			return 0;

		final int minRounds = Math.max(40, (int) (sUnits.length));
		final int maxRounds = Math.max(120, (int) (sUnits.length) * 6);
		final int popSize = minRounds * 3 / 2;
		final int numMetaRounds = 3;
		final int select = 5;

		ui.setTitle("");
		ui.setMaximum(numMetaRounds * maxRounds + minRounds + minRounds/10+1);
		ui.show();

		// the best solution of all runs are collected here
		Solution[] veryBest = new Solution[numMetaRounds * select * 3 / 2];
		init(veryBest, sUnits);

		// do numMetaRounds runs of the evolutionary algorithm
		for (int metaRound = 0; metaRound < numMetaRounds && !stopFlag; ++metaRound) {
			Solution[] best = new Solution[numMetaRounds * select * 3 / 2];
			init(best, sUnits);
			Solution[] population = new Solution[popSize];
			init(population, sUnits);
			select(population);

			double oldBest = Double.NEGATIVE_INFINITY;
			int improved = 0;
			// do one run of the evol. algo. terminate if max number of rounds is reached or if minimum
			// number of rounds is reached and the solution quality does not increase any more
			for (int round = 0; (round < minRounds || (round < maxRounds && improved <= minRounds / 5)) && !stopFlag; ++round) {
				if (population[0].evaluate() > oldBest)
					improved = 0;
				else
					improved++;
				if (best[0] != null)
					oldBest = best[0].evaluate();
				if (round % Math.ceil(minRounds / 10) == 0) {
					mutate(population, Math.min(1 / Math.log(round + 1), .2), 0);
				} else
					mutate(population, Math.min(.3 / Math.log(round + 1), .2), 0);

				recombine(population);

				select(population);
				best[best.length - 1] = population[0].clone();
				select(best);
				if (round % Math.ceil(minRounds / 10) == 0) {
					log.info(round + " 0: " + best[0].evaluate() + " " + population[0].evaluate() + " "
							+ population.length / 10 + ": " + population[population.length / 10].evaluate() + " "
							+ (population.length / 10 * 9) + " "
							+ population[population.length / 10 * 9].evaluate() + " " + (population.length - 1)
							+ ": " + population[population.length - 1].evaluate());
					ui.setProgress((metaRound > 0 ? best[metaRound - 1].evaluate() : "0") + " - "
							+ population[0].evaluate(), metaRound * maxRounds + round);
				}
			}
			log.info("***" + minRounds + " 0: " + population[0].evaluate() + " " + population.length / 10
					+ ": " + population[population.length / 10].evaluate() + " "
					+ (population.length / 10 * 9) + " " + population[population.length / 10 * 9].evaluate()
					+ " " + (population.length - 1) + ": " + population[population.length - 1].evaluate());
			// collect best solutions
			for (int i = 0; i < select; ++i) {
				veryBest[veryBest.length - 1 - metaRound * select - i] = population[i];
			}
		}

		// optimize popualation of best solutions
		select(veryBest);
		log.info(" 0: " + veryBest[0].evaluate() + " l/3: " + veryBest[veryBest.length / 3].evaluate()
				+ " " + (veryBest.length - 1) + ": " + veryBest[veryBest.length - 1].evaluate());
		for (int round = 0; round < minRounds * 2; ++round) {
			ui.setProgress("" + veryBest[0].evaluate(), numMetaRounds * maxRounds + round);
			mutate(veryBest, .1, 1);
			recombine(veryBest);
			select(veryBest);
		}
		select(veryBest);
		veryBest[0].assignTeachers();
		log.info("***** 0: " + veryBest[0].evaluate() + " l/3: "
				+ veryBest[veryBest.length / 3].evaluate() + " " + (veryBest.length - 1) + ": "
				+ veryBest[veryBest.length - 1].evaluate());
		ui.ready();

		if (veryBest.length == 0)
			return -1;
		// fix solution
		fix(veryBest[0]);
		// output
		return setResult(veryBest[0]);
	}

	/**
	 * Removes all orders containing $$$
	 */
	public void clear() {
		for (Unit u : units) {
			Collection<String> orders = new ArrayList<String>(u.getOrders());
			for (Iterator<String> it = orders.iterator(); it.hasNext();) {
				String o = (String) it.next();
				int start = o.indexOf("$$$");
				if (start != -1) {
					it.remove();
				}
			}
			u.setOrders(orders);
		}
	}

	private void init(Solution[] population, SUnit[] units) {
		for (int i = 0; i < population.length; ++i) {
			population[i] = new Solution(units);
		}
	}

	private void select(Solution[] population) {
		Arrays.sort(population);
	}

	private void mutate(Solution[] population, double d, int keepFirst) {
		// keep first individual?
		for (int i = keepFirst; i < population.length / 2; ++i) {
			population[i].mutate(d);
		}

	}

	private void recombine(Solution[] population) {
		int n = population.length * 2 / 3;
		int m = population.length - n;

		for (int i = 0; i < n; ++i) {
			int rand1 = random.nextInt(m);
			if (random.nextBoolean()) {
				int rand = random.nextInt(m * (m - 1) / 2);
				rand1 = m - (int) Math.floor(.5 + Math.sqrt(2 * rand));
				rand1 = Math.max(0, Math.min(population.length - 1, rand1));
			}
			int rand2 = random.nextInt(m);
			// log.info(rand1+" + "+rand2+" = "+(population.length - 1 - i));
			population[population.length - 1 - i].mate(population[rand1], population[rand2]);
		}
	}

	private void fix(Solution population) {
		population.fix();
	}

	/**
	 * Returns the type of order of the unit.
	 * 
	 * If there is an error, <code>null</code> is returned. If the unit has no LEHRE/LERNE order, an
	 * "teach all" order is returned.
	 * 
	 * @param u
	 * @return
	 */
	public static Order getCurrentOrder(Unit u) {
		List<String> orders = new ArrayList<String>(u.getOrders());
		Order value = null;
		for (Iterator<String> it = orders.iterator(); it.hasNext();) {
			String o = (String) it.next().trim();
			if (o.toUpperCase().startsWith("LEHRE")) {
				if (value != null) {
					return null;
				}
				value = new Order(o.substring(o.indexOf(" ")).trim().toLowerCase(), 0, true);
			}
			if (o.toUpperCase().startsWith("LERNE")) {
				if (value != null) {
					return null;
				}
				value = new Order(o.substring(o.indexOf(" ")).trim().toLowerCase(), 1, 1, 0);
			}
		}
		if (value == null)
			return new Order(0);

		return value;
	}

	/**
	 * Sets the orders according to the solution
	 * 
	 * @param best
	 * @return The value of the solution
	 */
	private double setResult(Solution best) {
		// boolean first = false;
		for (Solution.Info info : best.infos) {
			List<String> orders = new ArrayList<String>(info.getUnit().getOrders());
			for (Iterator<String> it = orders.iterator(); it.hasNext();) {
				String o = (String) it.next().trim();
				if (o.startsWith("LEHRE") || o.startsWith("LERNE")) {
					it.remove();
				}
			}
			info.getUnit().setOrders(orders);
		}
		StringBuffer[] orders = new StringBuffer[best.units.length];
		for (int i = 0; i < best.infos.length; ++i) {
			Solution.Info info = best.infos[i];
			if (info.learning != null)
				orders[i] = new StringBuffer("LERNEN " + info.learning);
			if (info.getTeacher() != -1)
				if (orders[info.getTeacher()] == null)
					orders[info.getTeacher()] = new StringBuffer("LEHRE " + info.getUnit().getID());
				else
					orders[info.getTeacher()].append(" " + info.getUnit().getID());
		}
		for (int i = 0; i < best.infos.length; ++i) {
			Solution.Info info = best.infos[i];
			if (info.learning == null)
				info.getUnit().addOrder("; $$$ T " + info.students, false, 0);
			else
				info.getUnit().addOrder("; $$$ L " + info.getTeacherSet().size(), false, 0);
			if (orders[i] == null)
				info.getUnit().addOrder("; $$$ teaching error", false, 0);
			else {
				info.getUnit().addOrder(orders[i].toString(), false, 0);
				if (info.learning == null) {
					// Lehrer
					if (info.students == info.getUnit().getModifiedPersons() * 10)
						if (isConfirmFullTeachers())
							info.getUnit().setOrdersConfirmed(true);
						else
							info.getUnit().setOrdersConfirmed(false);
					else if (isConfirmEmptyTeachers()
							&& info.students >= info.getUnit().getModifiedPersons() * 10 * getPercentFull()
									/ 100.)
						info.getUnit().setOrdersConfirmed(true);
					else
						info.getUnit().setOrdersConfirmed(false);
				} else {
					if (info.getTeacher() != -1 && isConfirmTaughtStudents())
						info.getUnit().setOrdersConfirmed(true);
					else if (info.getTeacher() == -1 && isConfirmUntaughtStudents())
						info.getUnit().setOrdersConfirmed(true);
					else
						info.getUnit().setOrdersConfirmed(false);
				}
			}
		}

		return best.evaluate();
	}

	public static void clear(Collection<Unit> units, String namespace) {
		(new Teacher(units, namespace, new NullUserInterface())).clear();

	}

	public static void parse(Collection<Unit> units, String namespace, ProgressBarUI ui) {
		(new Teacher(units, namespace, ui)).parse();
	}

	public static void untag(Collection<Unit> units, String namespace, ProgressBarUI ui) {
		(new Teacher(units, namespace, ui)).unTag();
	}

	public static void addOrder(Collection<Unit> units, String namespace, Order newOrder) {
		delOrder(units, namespace, newOrder);
		// add new meta order to all units
		for (Unit u : units) {
			Collection<String> oldOrders = u.getOrders();
			List<OrderList> relevantOrders = new ArrayList<OrderList>();
			List<String> newOrders = new ArrayList<String>(oldOrders.size());
			boolean foundSame = false;

			// look for matching meta order
			for (String line : oldOrders) {
				boolean isRelevant = false;
				OrderList orderList;
				try {
					orderList = parseOrder(u, line, getTeachTag(namespace), getLearnTag(namespace));
				} catch (OrderFormatException e) {
					orderList = new OrderList(Order.TEACH);
				}
				if (!orderList.orders.isEmpty() && orderList.getType().equals(newOrder.getType())) {
					isRelevant = true;
					relevantOrders.add(orderList);
				}
				if (!isRelevant) {
					newOrders.add(line);
				}
			}

			// add all meta orders
			boolean added = false;
			if (relevantOrders.isEmpty()) {
				relevantOrders.add(new OrderList(newOrder.getType()));
			}
			relevantOrders.get(0).addOrder(newOrder);

			for (OrderList orderList : relevantOrders) {
				newOrders.add(orderList.getName(namespace));
			}
			u.setOrders(newOrders);
		}
	}

	public static void delOrder(Collection<Unit> units, String namespace, Order newOrder) {
		delOrder(units, namespace, newOrder, false);
	}

	public static void delAllOrders(Collection<Unit> units, String namespace) {
		delOrder(units, namespace, null, true);
	}

	/**
	 * Delete orders matching newOrder from units orders. If <code>newOrder == null && safety</code>
	 * then delete <em>all</em> meta orders of all units.
	 * 
	 * 
	 * @param units
	 * @param namespace
	 * @param newOrder
	 * @param safety
	 */
	protected static void delOrder(Collection<Unit> units, String namespace, Order newOrder,
			boolean safety) {
		if (namespace == null)
			namespace = "";
		// add new L order to all units
		for (Unit u : units) {
			Collection<String> oldOrders = u.getOrders();
			List<String> newOrders = new ArrayList<String>(oldOrders.size());

			// look for L order
			for (String line : oldOrders) {

				OrderList orderList;
				try {
					orderList = parseOrder(u, line, getTeachTag(namespace), getLearnTag(namespace));
				} catch (OrderFormatException e) {
					orderList = new OrderList(Order.LEARN);
				}
				if (orderList.orders.isEmpty()) {
					newOrders.add(line);
				} else if (newOrder == null) {
					if (!safety) {
						throw new IllegalArgumentException(
								"you didn't intend to delete all meta orders, did you?");
					} else {
						// delete this line
					}
				} else {
					OrderList newOrderList = new OrderList(orderList.getType());
					for (Order order : orderList.orders) {
						if (!order.getType().equals(newOrder.getType())
								|| !order.getTalent().equals(newOrder.getTalent())) {
							newOrderList.addOrder(order);
						}
					}
					if (!newOrderList.orders.isEmpty()) {
						newOrders.add(newOrderList.getName(namespace));
					}
				}
			}
			u.setOrders(newOrders);
		}
	}

	private static String getLearnTag(String namespace) {
		if (namespace == null) {
			return "$L";
		} else {
			return "$" + namespace + "$L";
		}
	}

	private static String getTeachTag(String namespace) {
		if (namespace == null) {
			return "$T";
		} else {
			return "$" + namespace + "$T";
		}
	}

	public boolean isConfirmFullTeachers() {
		return confirmFullTeachers;
	}

	public void setConfirmFullTeachers(boolean confirmFullTeachers) {
		this.confirmFullTeachers = confirmFullTeachers;
	}

	public boolean isConfirmEmptyTeachers() {
		return confirmEmptyTeachers;
	}

	public void setConfirmEmptyTeachers(boolean confirmEmptyTeachers) {
		this.confirmEmptyTeachers = confirmEmptyTeachers;
	}

	public boolean isConfirmTaughtStudents() {
		return confirmTaughtStudents;
	}

	public void setConfirmTaughtStudents(boolean confirmTaughtStudents) {
		this.confirmTaughtStudents = confirmTaughtStudents;
	}

	public int getPercentFull() {
		return percentFull;
	}

	public void setPercentFull(int percentFull) {
		this.percentFull = percentFull;
	}

	public void stop() {
		stopFlag  = true;
	}

}
