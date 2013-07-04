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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import magellan.client.swing.ProgressBarUI;
import magellan.library.Skill;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.utils.NullUserInterface;
import magellan.library.utils.Resources;
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

	private Collection<Unit> units = Collections.emptyList();
	private Collection<String> namespaces;

	private boolean unconfirm = true;

	private boolean confirmFullTeachers = true;

	private boolean confirmEmptyTeachers = false;

	private boolean confirmTaughtStudents = true;

	private boolean confirmUntaughtStudents = false;

	private int percentFull;

	private int minDist;

	private double quality;

	private UserInterface ui;

	private SUnit[] sUnits;

	public static final String delim = " ";

	public static final int TEACH_DIFF = 2;

	Random random = new Random();

	private boolean stopFlag = false;

	// maps for efficient lookup
	Map<String, Integer> skillIndices = new HashMap<String, Integer>();
	ArrayList<String> skillNames = new ArrayList<String>();
	Map<Unit, Map<Integer, Integer>> skillMaps = new HashMap<Unit, Map<Integer, Integer>>();

	// map skill -> map level -> weight
	private Map<Integer, Map<Integer, Double>> globalWeights;

	private double minGlobalWeight = .7d;

	/**
	 * Create a new Teacher object.
	 * 
	 * @param units
	 *          The collection of units this Teacher manages.
	 * @param namespace
	 *          The namespace for orders
	 * @param ui
	 *          A user interface for feedback. May be <code>null</code>.
	 */
	Teacher(Collection<Unit> units, Collection<String> namespaces, UserInterface ui) {
		this.units = units;
		if (namespaces == null || namespaces.size() == 0) {
			this.namespaces = Collections.singletonList("");
		} else {
			this.namespaces = new LinkedList<String>(namespaces);
		}
		if (ui != null) {
			this.ui = ui;
		} else {
			this.ui = NullUserInterface.getInstance();
		}
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
			private Integer learning = null;
			private int students = 0;
			private int teacher = -1;

			public Info(SUnit unit) {
				this.unit = unit;
				assignLearn();
			}

			public void assignLearn() {
				int index = random.nextInt(unit.getLearnTalents().size());
				int count = 0;
				for (Integer talent : unit.getLearnTalents()) {
					if (count++ == index) {
						setLearning(talent);
						return;
					}
				}
			}

			@Override
			public String toString() {
				return unit.toString() + " " + getSkillName(getLearning()) + " " + students + " " + teacher;
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
				if (teacher == t) {
					teacher = -1;
				}
			}

			public int getNumTeachers() {
				if (teacher == -1) {
					return 0;
				}
				return infos[teacher].getUnit().getModifiedPersons();
			}

			public Collection<Integer> getTeacherSet() {
				if (teacher == -1) {
					return Collections.emptySet();
				} else {
					return Collections.singleton(teacher);
				}
			}

			public int getTeacher() {
				return teacher;
			}

			public int getRandomTeacher() {
				List<Integer> teachers = getSUnit().getTeachers();
				if (teachers.size() == 0) {
					return -1;
				}
				return teachers.get(random.nextInt(teachers.size()));
			}

			public int getFreeTeacher() {
				List<Integer> teachers = getSUnit().getTeachers();
				if (teachers.size() == 0) {
					return -1;
				}
				int firstTeacher = random.nextInt(teachers.size());
				for (int i = 0; i < teachers.size(); ++i) {
					if (validTeacher(this, teachers.get((firstTeacher + i) % teachers.size()))) {
						return teachers.get((firstTeacher + i) % teachers.size());
					}
				}

				return -1;
			}

			@Override
			public Info clone() {
				try {
					return (Info) super.clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
					return null;
				}
			}

			/**
			 * @param learning
			 *          the learning to set
			 */
			void setLearning(Integer learning) {
				this.learning = learning;
			}

			/**
			 * @return the learning
			 */
			Integer getLearning() {
				return learning;
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
			this(units, false);
		}

		public Solution(SUnit[] units, boolean initToCurrent) {
			this.units = units;
			init();
			if (initToCurrent) {
				adjustToCurrentOrders();
			}
		}

		private void adjustToCurrentOrders() {
			Map<String, Info> lookup = new HashMap<String, Info>();
			for (Info i : infos) {
				lookup.put(i.getUnit().getID().toString(), i);
			}
			for (Info info : infos) {
				Order o = getCurrentOrder(info.getUnit());
				if (o != null && o.isLearnOrder()) {
					info.setLearning(getSkillIndex(o.getTalent()));
					info.students = 0;
				}
			}
			for (Info info : infos) {
				Order o = getCurrentOrder(info.getUnit());
				if (o != null && o.isTeachOrder()) {
					info.setLearning(null);
					String s = getCurrentOrder(info.getUnit(), true);
					if (s != null) {
						StringTokenizer tokenizer = new StringTokenizer(s);
						tokenizer.nextElement(); // skip LEHRE
						while (tokenizer.hasMoreTokens()) {
							String id = tokenizer.nextToken();
							Info student = lookup.get(id);
							if (student != null && student.getLearning() != null
									&& validTeacher(student, info.getSUnit().getIndex())) {
								student.addTeacher(info.getSUnit().getIndex());
								info.students += student.getUnit().getModifiedPersons();
							}
						}
					}
				}
			}
		}

		void init() {
			infos = new Info[units.length];
			for (int i = 0; i < infos.length; ++i) {
				Info info = new Info(units[i]);
				infos[i] = info;
			}
			changed = true;
			result = 0;
		}

		@Override
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
			if (!changed) {
				return result;
			}
			result = 0;
			for (int i = 0; i < infos.length; ++i) {
				Info info = infos[i];
				result += evaluate(info);
			}
			changed = false;
			return result;
		}

		public double evaluate(Info info) {
			SUnit su = info.getSUnit();
			double value = 0;
			if (info.getLearning() != null) {
				// TODO modify value based on potential teachers in the region; a skill with many teachers
				// should only be learned with a teacher, otherwise a secondary skill should be learned
				value = su.calcWeight(info.getLearning());
				value *= su.getUnit().getModifiedPersons();
				value *= su.getPrio();
				int sLevel = su.getSkillLevel(info.getLearning());
				value *= 1 + Math.sqrt(sLevel) / 5.5;
				if (info.getNumTeachers() > 0) {
					Info teacher = infos[info.teacher];
					int tLevel = teacher.getSUnit().getSkillLevel(info.getLearning());
					int maxDiff = teacher.getSUnit().getMaximumDifference(info.getLearning());
					if (maxDiff == 1) {
						value = 0;
					} else if (tLevel - sLevel < TEACH_DIFF) {
						// log.warn("diff<2: " + teacher.getUnit() + " " + info.getUnit() + " "
						// + getSkillName(info.getLearning()));
						// value = 0;
					} else if (tLevel - sLevel < getMinDist()) {
						if (maxDiff != 0 && maxDiff < tLevel - sLevel) {
							value *= (1 + WRONGLEVEL_VALUE * sLevel / tLevel);
						} else {
							value *= (1 + WRONGLEVEL_VALUE);
						}
					} else if (maxDiff != 0 && maxDiff < tLevel - sLevel) {
						value *= (1 + WRONGLEVEL_VALUE * sLevel / tLevel);
					} else {
						value *= 2;
					}
				} else {
					value *= getGlobalWeight(info.getLearning(), sLevel);
				}
			}
			return value;
		}

		/**
		 * Returns a positive value if this solutions value is smaller than o's value.
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Solution o) {
			double e2 = (o).evaluate();
			double e1 = evaluate();

			return (e2 > e1 ? (int) (e2 - e1 + 1) : e2 == e1 ? 0 : (int) (e2 - e1 - 1));
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
						if (info.getLearning() == null) {
							// learn new talent
							info.assignLearn();
							info.clearTeachers();
							info.students = 0;
						} else {
							// become teacher
							info.setLearning(null);
							info.students = 0;
							if (info.getTeacher() != -1) {
								infos[info.getTeacher()].students -= info.getUnit().getModifiedPersons();
							}
							info.clearTeachers();
						}
					}

					// assign new teacher
					if (info.getLearning() != null && random.nextDouble() < teacherProb) {
						if (info.getTeacher() != -1) {
							infos[info.getTeacher()].students -= info.getUnit().getModifiedPersons();
						}
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
		public int fix() {
			int fixes = 0;
			for (int i = 0; i < infos.length; ++i) {
				Info info = infos[i];
				SUnit u = info.unit;
				if (info.students == 0) { // info.getLearning() == null &&
					Integer oldTalent = info.getLearning();
					Integer maxT = null;
					double max = evaluate(info);
					for (Integer t : u.getLearnTalents()) {
						info.setLearning(t);
						if (evaluate(info) > max) {
							max = evaluate(info);
							maxT = t;
						}
					}
					if (maxT != null && maxT != oldTalent) {
						// info.setLearning(oldTalent);
						// double e1 = evaluate(info);
						// info.setLearning(maxT);
						// double e2 = evaluate(info);
						// log.info("fixing " + u + " from " +
						// (oldTalent==null?"nothing":getSkillName(oldTalent))+ ", "+ e1 + " to "+
						// getSkillName(maxT)+", "+e2);
						info.setLearning(maxT);
						changed = true;
						fixes++;
					} else {
						info.setLearning(oldTalent);
					}
				}
			}
			return fixes;
		}

		/**
		 * Tries to assign a teacher to every unit not already having one.
		 */
		protected void assignTeachers() {
			Collection<Integer> teacherSet = new ArrayList<Integer>();

			// reset teachers
			for (int i = 0; i < infos.length; ++i) {
				Info info = infos[i];
				if (info.getLearning() == null) {
					teacherSet.add(new Integer(i));
				}
				if (info.getTeacher() != -1 && infos[info.getTeacher()].getLearning() != null) {
					info.clearTeachers();
				}

			}

			if (teacherSet.size() <= 0) {
				return;
			}

			// Integer[] teachers = (Integer[]) teacherSet.toArray(new Integer[0]);

			// assign teachers
			for (int i = 0; i < infos.length; ++i) {
				Info student = infos[i];
				if (student.getLearning() != null) {
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
				changed = true;
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
		 * Combines two solutions resulting in a new solution similar to the two old ones.
		 * 
		 * @param solution
		 * @param solution2
		 */
		public void mate(Solution solution, Solution solution2) {
			if (solution.units != solution2.units) {
				throw new IllegalArgumentException("incompatible solutions");
			}
			init();

			// int crossoverPoint1 = random.nextInt(infos.length-1);
			// this seemed to work better than a one point crossover
			byte[] parents = new byte[infos.length];
			random.nextBytes(parents);

			// first step: only assign "roles"
			for (int i = 0; i < infos.length; ++i) {
				Info info = infos[i];
				Info parent;
				if (parents[i] % 2 == 0) {
					// if (i<=crossoverPoint1)// || (i>crossoverPoint2 && i<=crossoverPoint3) ||
					// (i>crossoverPoint4))
					parent = solution.infos[i];
				} else {
					parent = solution2.infos[i];
				}
				info.setLearning(parent.getLearning());
				info.students = 0;
				info.clearTeachers();
			}

			// assign teachers
			for (int i = 0; i < infos.length; ++i) {
				Info student = infos[i];
				Info parent;
				if (parents[i] % 2 == 0) {
					// if (i<=crossoverPoint1)// || (i>crossoverPoint2 && i<=crossoverPoint3) ||
					// (i>crossoverPoint4))
					parent = solution.infos[i];
				} else {
					parent = solution2.infos[i];
				}
				if (student.getLearning() != null && parent.getTeacher() != -1) {
					if (validTeacher(student, parent.getTeacher())) {
						assignTeacher(student, parent.getTeacher());
					}
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
			boolean result = false;
			if (teacher.getLearning() != null
					|| teacher.getSUnit().getSkillLevel(student.getLearning()) < student.getSUnit()
							.getSkillLevel(student.getLearning()) + TEACH_DIFF) {
				result = false;
			} else {
				result = partial
						|| teacher.getUnit().getModifiedPersons() * 10 >= teacher.students
								+ student.getUnit().getModifiedPersons();
			}
			return result;
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

	public Integer getSkillIndex(String talent) {
		talent = talent.toLowerCase();
		Integer index = skillIndices.get(talent);
		if (index == null) {
			index = skillNames.size();
			skillNames.add(talent);
			skillIndices.put(talent, index);
		}
		return index;
	}

	public String getSkillName(Integer skill) {
		if (skill == null || skill >= skillNames.size()) {
			throw new IllegalArgumentException();
		}
		return skillNames.get(skill);
	}

	/**
	 * Returns the skill level of a unit. For example getLevel(unit,"Unterhaltung")
	 */
	public int getLevel(Unit unit, Integer skill) {
		Map<Integer, Integer> skills = skillMaps.get(unit);
		if (skills == null) {
			skills = new HashMap<Integer, Integer>();
			skillMaps.put(unit, skills);
		}
		Integer level = skills.get(skill);
		if (level == null) {
			level = getLevel(unit, getSkillName(skill));
			skills.put(skill, level);
		}
		return level;
	}

	/**
	 * Extracts the teaching units from the orders.
	 * 
	 * @param setTags
	 *          if <code>true</code>, the units get tags for their highest teach and learn skills.
	 * 
	 * @return A List of units who are teachers or students
	 */
	public Collection<SUnit> getUnits(boolean setTags) {

		Collection<SUnit> result = new ArrayList<SUnit>(units.size());
		for (Unit u : units) {
			SUnit su = parseUnit(u, setTags);
			if (su != null) {
				result.add(su);
				su.setIndex(result.size() - 1);
			}
		}
		for (SUnit student : result) {
			for (SUnit teacher : result) {
				if (valid(student, teacher)) {
					student.addTeacher(teacher.getIndex());
				}
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
		for (Integer talent : student.getLearnTalents()) {
			int diff = teacher.getMaximumDifference(talent);

			if (student.getTarget(talent) > 0 && diff != 1
			// && getLevel(teacher.getUnit(), talent) - getLevel(student.getUnit(), talent)
			// <=diff
					&& getLevel(teacher.getUnit(), talent) - getLevel(student.getUnit(), talent) >= TEACH_DIFF) {
				result = true;
				break;
			}
		}
		return result;
	}

	public static SUnit parseUnit(Unit unit, Collection<String> namespaces, boolean setTags) {
		return (new Teacher(Collections.singletonList(unit), namespaces, null))
				.parseUnit(unit, setTags);
	}

	/**
	 * Parse the orders of <code>u</code>.
	 * 
	 * @param u
	 * @return A {@link SUnit} according to <code>u</code>'s orders, <code>null</code> if this unit
	 *         has no teaching or learning orders
	 */
	public SUnit parseUnit(Unit u, boolean setTags) {
		SUnit su = null;
		boolean errorFlag = false;
		for (String orderLine : u.getOrders()) {
			try {
				OrderList orderList = null;
				orderList = parseOrder(u, orderLine, namespaces);
				for (Order order : orderList.orders) {
					String talent = order.getTalent();
					if (order.isTeachOrder()) {
						int diff = order.getDiff();
						if (su == null) {
							su = new SUnit(this, u);
						}
						if (talent.equals(Order.ALL)) {
							if (u.getModifiedSkills().isEmpty()) {
								continue;
							}

							// add all skills weighted by their level
							for (Skill s : u.getModifiedSkills()) {
								su.addTeach(getSkillIndex(s.getName()), diff);
							}
						} else {
							su.addTeach(getSkillIndex(talent), diff);
						}
					} else {
						int target = order.getTarget();
						int max = order.getMax();
						if (su == null) {
							su = new SUnit(this, u);
						}
						if (talent.equals(Order.ALL)) {
							throw new IllegalArgumentException("L ALL not supported");
						} else {
							su.addLearn(getSkillIndex(talent), target, max);
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
			if (setTags) {
				u.putTag(LEARN_TAG, "error");
			}
			if (errorFlag) {
				u.addOrder("; $$$ teach error: syntax error", false, 0);
			} else {
				u.addOrder("; $$$ teach error: unit needs L order", false, 0);
			}
			return null;
		}

		if (setTags && su != null) {
			su.attachTags();
		}
		return su;
	}

	/**
	 * Tries to parse orderLine.
	 * 
	 * @param orderLine
	 * @param teachTag
	 * @param learnTag
	 * @return
	 */
	protected static OrderList parseOrder(Unit unit, String orderLine, Collection<String> namespaces) {
		for (String nsp : namespaces) {
			OrderList result = null;
			String teachTag = getTeachTag(nsp);
			String learnTag = getLearnTag(nsp);
			try {
				// try to find out which kind of order we have
				int start = orderLine.indexOf(teachTag);
				if (start != -1) {
					// teach order
					result = new OrderList(Order.TEACH, nsp);
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
						result = new OrderList(Order.LEARN, nsp);
						StringTokenizer st = new StringTokenizer(
								orderLine.substring(start + teachTag.length()), delim, false);
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
								if (talent == null) {
									talent = st.nextToken();
								}
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
				// throw new OrderFormatException("parse error in line " + orderLine, e);
			}
			if (result != null && result.orders.size() > 0) {
				return result;
			}
		}
		return new OrderList(Order.LEARN, namespaces.iterator().next());
	}

	public static class OrderList {
		public List<Order> orders = new ArrayList<Order>(1);
		String type = null;
		String namespace;

		private double prio = 1;

		public OrderList(String type, String namespace) {
			if (type == null || namespace == null) {
				throw new NullPointerException();
			}
			this.type = type;
			this.namespace = namespace;
		}

		public void addOrder(Order o) {
			orders.add(o);
			if (type.equals(Order.LEARN)) {
				setPrio(Math.max(getPrio(), o.getPrio()));
			}
		}

		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		public void setPrio(double prio) {
			for (Order o : orders) {
				o.setPrio(prio);
			}
			this.prio = prio;

		}

		@Override
		public String toString() {
			return getName();
		}

		/**
		 * @return the prio
		 */
		public double getPrio() {
			return prio;
		}

		/**
		 * Returns an order string for this order list like
		 * <code>"// $stm$L 100 Stangenwaffen 10 99 Ausdauer 5 99</code> for learn orders and
		 * <code>"// $stm$T Hiebwaffen 2</code> for teach orders. Here, <code>stm</code> is the
		 * namespace.
		 * 
		 * @param namespace
		 * @return
		 */
		public String getName() {
			StringBuffer sb = new StringBuffer();
			sb.append("// ");
			if (type.equals(Order.TEACH)) {
				sb.append(getTeachTag(namespace));
			} else {
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

		public String getNamespace() {
			return namespace;
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
		if (result.getPrio() < talentPrio) {
			result.setPrio(talentPrio);
		}

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
		if (unit.getModifiedSkills().isEmpty()) {
			return;
		}

		// find min an max skill
		int maxSkill = 1, minSkill = Integer.MAX_VALUE;
		for (Skill s : unit.getModifiedSkills()) {
			int l = s.getLevel();
			if (l > maxSkill) {
				maxSkill = l;
			}
			if (l < minSkill) {
				minSkill = l;
			}
		}
		// add all skills weighted by their level
		for (Skill s : unit.getModifiedSkills()) {
			result.addOrder(new Order(s.getName(), talentPrio, Math.max(1, s.getLevel()), 999));
		}
	}

	public static void convert(Collection<Unit> values, Collection<String> namespaces) {
		for (Unit u : values) {
			Collection<String> newOrders = new ArrayList<String>(u.getOrders().size());
			boolean changed = false;
			for (String oldOrder : u.getOrders()) {
				OrderList orderList;
				orderList = parseOrder(u, oldOrder, namespaces);
				if (orderList.orders.isEmpty()) {
					newOrders.add(oldOrder);
				} else {
					changed = true;
					newOrders.add(orderList.getName());
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
			parseUnit(u, true);
		}
	}

	/**
	 * Removes tags from all units.
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
		sUnits = getUnits(false).toArray(new SUnit[0]);
		if (sUnits.length == 0) {
			return 0;
		}

		log.info("teaching " + sUnits.length + " units in namespace \"" + namespaces.toString() + "\"");

		final int minRounds = (int) Math.max(2,
				Math.round(getQuality() * Math.max(40, (sUnits.length * 3 / 2))));
		final int maxRounds = (int) Math.max(4,
				Math.round(getQuality() * Math.max(120, (sUnits.length) * 6)));
		final int popSize = Math.max(10, minRounds * 5 / 4);
		final int numMetaRounds = 3;
		final int numPreSolvedRounds = 2;
		final int select = 5;

		ui.setMaximum(numMetaRounds * maxRounds + minRounds + minRounds / 10 + 1);
		// ui.show();

		// the best solution of all runs are collected here
		Solution[] veryBest = new Solution[numMetaRounds * select * 3 / 2];
		if (veryBest.length == 0) {
			veryBest = new Solution[1];
		}
		init(veryBest, sUnits, true);

		// do numMetaRounds runs of the evolutionary algorithm
		for (int metaRound = 0; metaRound < numMetaRounds && !stopFlag; ++metaRound) {
			Solution[] best = new Solution[numMetaRounds * select * 3 / 2];
			init(best, sUnits, false);
			Solution[] population = new Solution[popSize]; // * (metaRound<numPreSolvedRounds?30:60)/60];
			init(population, sUnits, metaRound < numPreSolvedRounds);
			// log.info(fix(population)+" fixes ");
			select(population);

			double oldBest = Double.NEGATIVE_INFINITY;
			int notImproved = 0;
			// do one run of the evol. algo; terminate if max number of rounds is reached or if minimum
			// number of rounds is reached and the solution quality does not increase any more
			log.fine("run " + metaRound + "/" + numMetaRounds);
			int round = 0;
			for (; (round < minRounds || (round < maxRounds && notImproved <= Math.max(3, minRounds / 5)))
					&& !stopFlag; ++round) {
				if (population[0].evaluate() > oldBest) {
					notImproved = 0;
				} else {
					notImproved++;
				}
				if (best[0] != null) {
					oldBest = best[0].evaluate();
				}

				// /////// mutate (sometimes more, sometimes less)
				if (round % Math.ceil(minRounds / 10.) == 0) {
					mutate(population, Math.min(1 / Math.log(round + 1), .2), numMetaRounds - metaRound - 1);
				} else {
					mutate(population, Math.min(.3 / Math.log(round + 1), .2), numMetaRounds - metaRound - 1);
				}

				// ////// recombine
				recombine(population);

				// ////// select
				select(population);

				// ///// fix some solutions (not all, to preserve randomness...)
				for (int fixed = 0; fixed < population.length; fixed += 2) {
					population[fixed].fix();
				}

				// remember best solutions
				best[best.length - 1] = population[0].clone();
				select(best);
				if (round == 1 || round % Math.ceil(minRounds / 6.) == 0) {
					log.fine("round " + round + "/" + minRounds + " best: " + best[0].evaluate() + " 0: "
							+ population[0].evaluate() + " " + population.length / 10 + ": "
							+ population[population.length / 10].evaluate() + " " + (population.length / 10 * 9)
							+ ": " + population[population.length / 10 * 9].evaluate() + " "
							+ (population.length - 1) + ": " + population[population.length - 1].evaluate());
					ui.setProgress((metaRound > 0 ? best[metaRound - 1].evaluate() : "0") + " - "
							+ population[0].evaluate(), metaRound * maxRounds + round);
				}
			}

			// collect best solutions
			for (int i = 0; i < select - 1; ++i) {
				veryBest[veryBest.length - 1 - metaRound * select - i] = population[i];
			}
			veryBest[veryBest.length - 1 - metaRound * select - (select - 1)] = best[0];
			log.fine("***" + minRounds + "/" + round + "/" + maxRounds + " best: " + best[0].evaluate()
					+ " 0: " + population[0].evaluate() + " " + population.length / 10 + ": "
					+ population[population.length / 10].evaluate() + " " + (population.length / 10 * 9)
					+ ": " + population[population.length / 10 * 9].evaluate() + " "
					+ (population.length - 1) + ": " + population[population.length - 1].evaluate());
		}

		// fix all remaining
		log.fine(fix(veryBest) + " fixes ");

		// optimize population of best solutions
		select(veryBest);
		log.fine("population:  0: " + veryBest[0].evaluate() + " l/3: "
				+ veryBest[veryBest.length / 3].evaluate() + " " + (veryBest.length - 1) + ": "
				+ veryBest[veryBest.length - 1].evaluate());
		stopFlag = false;
		log.fine("refinement");
		for (int round = 0; round < minRounds * 8 && !stopFlag; ++round) {
			if (round % (2 * minRounds) == 0) {
				log.fine("round " + round + "/" + minRounds * 8 + ": " + veryBest[0].evaluate() + " l/3: "
						+ veryBest[veryBest.length / 3].evaluate() + " " + (veryBest.length - 1) + ": "
						+ veryBest[veryBest.length - 1].evaluate());
			}
			if (round > 0 && round % (minRounds * 4) == 0) {
				log.fine(fix(veryBest) + " fixes ");
			}
			ui.setProgress("" + veryBest[0].evaluate(), numMetaRounds * maxRounds + round);
			mutate(veryBest, .05 + .1 * (1 - Math.min(1, .2 * round / minRounds)), 1);
			recombine(veryBest);
			select(veryBest);
		}

		// select and return winner
		select(veryBest);
		log.fine("**best population:**  0: " + veryBest[0].evaluate() + " l/3: "
				+ veryBest[veryBest.length / 3].evaluate() + " " + (veryBest.length - 1) + ": "
				+ veryBest[veryBest.length - 1].evaluate());
		log.fine(fix(veryBest) + " fixes ");
		select(veryBest);
		log.info("**best population:** 0: " + veryBest[0].evaluate() + " l/3: "
				+ veryBest[veryBest.length / 3].evaluate() + " " + (veryBest.length - 1) + ": "
				+ veryBest[veryBest.length - 1].evaluate());

		if (veryBest.length == 0) {
			return -1;
		}

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
				String o = it.next();
				int start = o.indexOf("$$$");
				if (start != -1) {
					it.remove();
				}
			}
			u.setOrders(orders);
		}
	}

	/**
	 * Initialize the population with random solutions. If <code>current == true</code> some solutions
	 * will be the solution implied by the current orders of the units.
	 * 
	 * @param population
	 * @param units
	 * @param current
	 */
	protected void init(Solution[] population, SUnit[] units, boolean current) {
		// log.info(population.length + " " + units.length + " " + current);
		for (int i = 0; i < population.length; ++i) {
			population[i] = new Solution(units);
		}
		try {
			if (current) {
				for (int i = 0; i < population.length && i < Math.max(1, Math.log(population.length) - 3); ++i) {
					population[i] = new Solution(units, true);
				}
			}
		} catch (Exception e) {
			log.error("orders foul: " + e);
			e.printStackTrace();
		}
		initGlobalWeight(units);
	}

	private void select(Solution[] population) {
		Arrays.sort(population);
	}

	private void mutate(Solution[] population, double prob, int keepFirst) {
		// keep first individual?
		for (int i = keepFirst; i < population.length / 2; ++i) {
			population[i].mutate(prob);
		}

	}

	private void recombine(Solution[] population) {
		// n new individuals
		int n = population.length * 2 / 3;
		// m individuals have higher chances of reproduction
		int m = population.length - n;

		for (int i = 0; i < n; ++i) {
			int rand1 = random.nextInt(m);
			if (random.nextBoolean()) {
				int rand = random.nextInt(m * (m - 1) / 2);
				rand1 = m - (int) (.5 + Math.sqrt(2 * rand));
				rand1 = Math.max(0, Math.min(population.length - 1, rand1));
			}
			int rand2 = random.nextInt(m);
			// log.info(rand1+" + "+rand2+" = "+(population.length - 1 - i));
			population[population.length - 1 - i].mate(population[rand1], population[rand2]);
		}
	}

	private int fix(Solution[] veryBest) {
		int fixes = 0;
		for (Solution s : veryBest) {
			fixes += s.fix();
		}
		return fixes;
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
		for (String string : orders) {
			String o = string.trim();
			if (isTeachOrder(o, u)) {
				if (value != null) {
					return null;
				}
				if (o.indexOf(" ") >= 0) {
					String argument = o.substring(o.indexOf(" ")).trim().toLowerCase();
					if (argument.trim().length() > 0) {
						value = new Order(argument, 0, true);
					}
				}

			}
			if (isLearnOrder(o, u)) {
				if (value != null) {
					return null;
				}
				if (o.indexOf(" ") >= 0) {
					String argument = o.substring(o.indexOf(" ")).trim().toLowerCase();
					if (argument.trim().length() > 0) {
						value = new Order(o.substring(o.indexOf(" ")).trim().toLowerCase(), 1, 1, 0);
					}
				}
			}
		}
		if (value == null) {
			return new Order(0);
		}

		return value;
	}

	private String getLearnOrder(Unit u) {
		Locale locale = u.getFaction().getLocale();
		return Resources.getOrderTranslation(EresseaConstants.O_LEARN, locale);
	}

	private String getTeachOrder(Unit u) {
		Locale locale = u.getFaction().getLocale();
		return Resources.getOrderTranslation(EresseaConstants.O_TEACH, locale);
	}

	private static boolean isLearnOrder(String o, Unit u) {
		Locale locale = u.getFaction().getLocale();
		return (o.toLowerCase(locale).trim().startsWith(Resources.getOrderTranslation(
				EresseaConstants.O_LEARN, locale).toLowerCase(locale)));
	}

	private static boolean isTeachOrder(String o, Unit u) {
		Locale locale = u.getFaction().getLocale();
		return (o.toLowerCase(locale).trim().startsWith(Resources.getOrderTranslation(
				EresseaConstants.O_TEACH, locale).toLowerCase(locale)));
	}

	private Object getLocalizedSkillName(Integer learning, Unit u) {
		Locale locale = u.getFaction().getLocale();
		StringBuffer skill = new StringBuffer(getSkillName(learning));
		skill.replace(0, 1, skill.substring(0, 1).toUpperCase());
		skill.insert(0, "rules.skill.");
		return Resources.get(skill.toString(), locale, true);
	}

	/**
	 * Returns the type of order of the unit.
	 * 
	 * If there is an error, <code>null</code> is returned. If the unit has no LEHRE/LERNE order,
	 * <code>null</code> is returned.
	 * 
	 * @param u
	 * @return
	 */
	public static String getCurrentOrder(Unit u, boolean flag) {
		List<String> orders = new ArrayList<String>(u.getOrders());
		String value = null;
		for (String string : orders) {
			String o = string.trim();
			if (isTeachOrder(o, u)) {
				if (value != null) {
					return null;
				}
				// FIXME syntax error gets OutOfBoundsException if Befehl.equals("LEHRE")
				value = o;
			}
			if (isLearnOrder(o, u)) {
				if (value != null) {
					return null;
				}
				value = o;
			}
		}

		return value;
	}

	/**
	 * Sets the orders according to the solution
	 * 
	 * @param best
	 * @return The value of the solution
	 */
	private double setResult(Solution best) {
		// find old teach and learn orders, and replace with comment for each
		for (Solution.Info info : best.infos) {
			List<String> orders = new ArrayList<String>(info.getUnit().getOrders());
			List<String> toAdd = new ArrayList<String>();
			for (Iterator<String> it = orders.iterator(); it.hasNext();) {
				String o = it.next().trim();
				if (isTeachOrder(o, info.getUnit()) || isLearnOrder(o, info.getUnit())) {
					toAdd.add("; $$$ " + o);
					it.remove();
				}
			}
			for (String newOrder : toAdd) {
				orders.add(newOrder);
			}
			info.getUnit().setOrders(orders);
		}

		// add new order according to best
		StringBuffer[] orders = new StringBuffer[best.units.length];
		for (int i = 0; i < best.infos.length; ++i) {
			Solution.Info info = best.infos[i];
			if (info.getLearning() != null) {
				orders[i] = new StringBuffer(getLearnOrder(info.getUnit()));
				orders[i].append(" ");
				orders[i].append(getLocalizedSkillName(info.getLearning(), info.getUnit()));
			}
			if (info.getTeacher() != -1) {
				if (orders[info.getTeacher()] == null) {
					orders[info.getTeacher()] = new StringBuffer(
							getTeachOrder(best.infos[info.getTeacher()].getUnit()));
					if (info.getUnit() instanceof TempUnit) {
						orders[info.getTeacher()].append(" ").append(
								Resources.getOrderTranslation(EresseaConstants.O_TEMP));
					}
					orders[info.getTeacher()].append(" ");
					orders[info.getTeacher()].append(info.getUnit().getID());
				} else {
					if (info.getUnit() instanceof TempUnit) {
						orders[info.getTeacher()].append(" ").append(
								Resources.getOrderTranslation(EresseaConstants.O_TEMP));
					}
					orders[info.getTeacher()].append(" ").append(info.getUnit().getID().toString());
				}
			}
		}

		// add debug information
		for (int i = 0; i < best.infos.length; ++i) {
			Solution.Info info = best.infos[i];
			StringBuilder debug = new StringBuilder();
			Integer learning = info.getLearning();
			int teacher = info.teacher;
			if (info.getLearning() == null) {
				debug.append("; $$$ T ").append(info.students);
			} else {
				debug.append("; $$$ L ").append(info.getTeacherSet().size());
			}
			for (Integer skill : info.getSUnit().getLearnTalents()) {
				if (debug.length() > 79) {
					info.getUnit().addOrder(debug.toString());
					debug.delete(0, debug.length());
					debug.append("; $$$   ");
				} else {
					debug.append(" ");
				}
				debug.append(getSkillName(skill)).append(" ")
						.append(String.format("%.3f", info.getSUnit().calcWeight(skill)));
				// info.setLearning(skill);
				// info.teacher = -1;
				// debug.append(" ").append(
				// String.format("%.3f", best.evaluate(info) / info.getUnit().getPersons()));
				// info.setLearning(learning);
				// info.teacher = teacher;
				debug.append(" ").append(
						String.format("%.3f", getGlobalWeight(skill, getLevel(info.getUnit(), skill))));
			}
			debug.append(" ").append(
					String.format("%.3f", best.evaluate(info) / info.getUnit().getPersons()));

			info.getUnit().addOrder(debug.toString());
			if (orders[i] == null) {
				info.getUnit().addOrder("; $$$ teaching error", false, 0);
			} else {
				info.getUnit().addOrder(orders[i].toString(), false, 0);
				// don't confirm units which were set to unconfirm by E3CommandParser
				if (info.getUnit().getTag("$cript.confirm") == null
						|| info.getUnit().getTag("$cript.confirm").equals("1")) {
					if (info.getLearning() == null) {
						// confirm according to settings
						if (info.students == info.getUnit().getModifiedPersons() * 10) {
							// full teacher
							if (isConfirmFullTeachers()) {
								info.getUnit().setOrdersConfirmed(true);
							} else if (isUnconfirm()) {
								info.getUnit().setOrdersConfirmed(false);
							}
						} else if (isConfirmEmptyTeachers()
								&& info.students >= info.getUnit().getModifiedPersons() * 10 * getPercentFull()
										/ 100.) {
							// partially full teacher
							info.getUnit().setOrdersConfirmed(true);
						} else if (isUnconfirm()) {
							info.getUnit().setOrdersConfirmed(false);
						}
					} else {
						if (info.getTeacher() != -1 && isConfirmTaughtStudents()) {
							// student with teacher
							info.getUnit().setOrdersConfirmed(true);
						} else if (info.getTeacher() == -1 && isConfirmUntaughtStudents()) {
							// student without teacher
							info.getUnit().setOrdersConfirmed(true);
						} else if (isUnconfirm()) {
							info.getUnit().setOrdersConfirmed(false);
						}
					}
				}
			}
		}

		return best.evaluate();
	}

	public static void clear(Collection<Unit> units, Collection<String> namespaces) {
		(new Teacher(units, namespaces, new NullUserInterface())).clear();

	}

	public static void parse(Collection<Unit> units, Collection<String> namespaces, ProgressBarUI ui) {
		(new Teacher(units, namespaces, ui)).parse();
	}

	public static void untag(Collection<Unit> units, Collection<String> namespaces, ProgressBarUI ui) {
		(new Teacher(units, namespaces, ui)).unTag();
	}

	public static void addOrder(Collection<Unit> units, String namespace, Order newOrder) {
		// replace order
		delOrder(units, Collections.singletonList(namespace), newOrder);
		// add new meta order to all units
		for (Unit u : units) {
			Collection<String> oldOrders = u.getOrders();
			List<OrderList> relevantOrders = new ArrayList<OrderList>();
			List<String> newOrders = new ArrayList<String>(oldOrders.size());

			// look for matching meta order
			for (String line : oldOrders) {
				boolean isRelevant = false;
				OrderList orderList;
				orderList = parseOrder(u, line, Collections.singletonList(namespace));
				if (!orderList.orders.isEmpty() && orderList.getType().equals(newOrder.getType())) {
					isRelevant = true;
					relevantOrders.add(orderList);
				}
				if (!isRelevant) {
					newOrders.add(line);
				}
			}

			// add all meta orders
			if (relevantOrders.isEmpty()) {
				relevantOrders.add(new OrderList(newOrder.getType(), namespace));
			}
			relevantOrders.get(0).addOrder(newOrder);

			for (OrderList orderList : relevantOrders) {
				newOrders.add(orderList.getName());
			}
			u.setOrders(newOrders);
		}
	}

	public static void delOrder(Collection<Unit> units, Collection<String> namespaces, Order newOrder) {
		delOrder(units, namespaces, newOrder, false);
	}

	public static void delAllOrders(Collection<Unit> units, Collection<String> namespaces) {
		delOrder(units, namespaces, null, true);
	}

	/**
	 * Sets the priority of all learn orders of the units to newPrio.
	 * 
	 * @param units
	 * @param namespace
	 * @param newPrio
	 */
	public static void setPrio(Collection<Unit> units, Collection<String> namespace, double newPrio) {
		for (Unit u : units) {
			Collection<String> oldOrders = u.getOrders();
			List<String> newOrders = new ArrayList<String>(oldOrders.size());

			for (String line : oldOrders) {

				OrderList orderList;
				// try to parse the old order
				orderList = parseOrder(u, line, namespace);
				if (orderList.orders.isEmpty()) {
					// just keep the old line
					newOrders.add(line);
				} else {
					// change priority of order
					if (orderList.getType().equals(Order.LEARN)) {
						orderList.setPrio(newPrio);
					}
					newOrders.add(orderList.getName());
				}
			}
			u.setOrders(newOrders);
		}
	}

	/**
	 * Deletes orders matching newOrder from units orders. If <code>newOrder == null && safety</code>
	 * then delete <em>all</em> meta orders of all units.
	 * 
	 * 
	 * @param units
	 * @param namespace
	 * @param newOrder
	 * @param safety
	 */
	protected static void delOrder(Collection<Unit> units, Collection<String> namespaces,
			Order newOrder, boolean safety) {
		for (Unit u : units) {
			Collection<String> oldOrders = u.getOrders();
			List<String> newOrders = new ArrayList<String>(oldOrders.size());

			// iterate through the unit's orders
			for (String line : oldOrders) {

				// try to parse the order
				OrderList orderList;
				orderList = parseOrder(u, line, namespaces);
				if (orderList.orders.isEmpty()) {
					// no order: just keep the old line
					newOrders.add(line);
				} else if (newOrder == null) {
					if (!safety) {
						throw new IllegalArgumentException(
								"you didn't intend to delete all meta orders, did you?");
					} else {
						// delete this line
					}
				} else {
					// iterate through the sub-orders of this line
					OrderList newOrderList = new OrderList(orderList.getType(), orderList.getNamespace());
					for (Order order : orderList.orders) {
						if (!order.getType().equals(newOrder.getType())
								|| !order.getTalent().equalsIgnoreCase(newOrder.getTalent())) {
							// order does not match newOrder: keep it
							newOrderList.addOrder(order);
						} // else delete order
					}
					// add result to new unit orders
					if (!newOrderList.orders.isEmpty()) {
						newOrders.add(newOrderList.getName());
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

	public boolean isConfirmUntaughtStudents() {
		return confirmUntaughtStudents;
	}

	public void setConfirmUntaughtStudents(boolean confirmUntaughtStudents) {
		this.confirmUntaughtStudents = confirmUntaughtStudents;
	}

	public int getPercentFull() {
		return percentFull;
	}

	public void setPercentFull(int percentFull) {
		this.percentFull = percentFull;
	}

	public void stop() {
		stopFlag = true;
	}

	public boolean isUnconfirm() {
		return unconfirm;
	}

	public void setUnconfirm(boolean unconfirm) {
		this.unconfirm = unconfirm;
	}

	public void setMinDist(int minDist) {
		this.minDist = minDist;
	}

	public int getMinDist() {
		return minDist;
	}

	/**
	 * @param quality
	 *          the quality to set
	 */
	public void setQuality(double quality) {
		this.quality = quality;
	}

	/**
	 * @return the quality
	 */
	public double getQuality() {
		return quality;
	}

	public double getGlobalWeight(Integer skill, int level) {
		if (globalWeights.get(skill) == null) {
			log.warnOnce("skill " + getSkillName(skill) + " (" + skill + ") unknown");
			return .1;
		}
		Double weight = globalWeights.get(skill).get(level);
		if (weight == null) {
			return 1;
		}
		return weight;
	}

	static final double WEIGHT_FACTOR1 = .2;
	static final double WEIGHT_FACTOR2 = .6;

	protected void initGlobalWeight(SUnit[] units2) {
		globalWeights = new HashMap<Integer, Map<Integer, Double>>();
		// map skill -> list level -> persons
		Map<Integer, ArrayList<Integer>> counts = new HashMap<Integer, ArrayList<Integer>>();

		for (SUnit unit : units2) {
			for (Integer skill : unit.getLearnTalents()) {
				ArrayList<Integer> count = counts.get(skill);
				if (count == null) {
					counts.put(skill, count = new ArrayList<Integer>());
				}
				increase(counts.get(skill), unit.getSkillLevel(skill), unit.getUnit().getPersons());
			}
		}

		for (Integer skill : counts.keySet()) {
			ArrayList<Integer> persons = counts.get(skill);
			Map<Integer, Double> skillWeights = new HashMap<Integer, Double>();
			globalWeights.put(skill, skillWeights);
			int teachers = 0;
			for (int level = persons.size() - 1; level >= TEACH_DIFF - 1; --level) {
				teachers += persons.get(level);
			}
			int students = 0;
			for (int level = 0; level < persons.size(); ++level) {
				students += persons.get(level);
				if (students > 0) {
					if (teachers * 2 >= persons.get(level)) {
						// more than enough teachers
						skillWeights.put(level, minGlobalWeight);
					} else if (teachers * 5 >= persons.get(level)) {
						// twice as much as needed
						skillWeights.put(level, 1 - (1 - minGlobalWeight) * WEIGHT_FACTOR2);
					} else if (teachers * 10 >= persons.get(level)) {
						// just enough teachers
						skillWeights.put(level, 1 - (1 - minGlobalWeight) * WEIGHT_FACTOR1);
					} else {
						// not enough
						skillWeights.put(level, 1 + (1 - minGlobalWeight) * WEIGHT_FACTOR1);
					}
				}
				if (level + TEACH_DIFF - 1 < persons.size()) {
					students = Math.max(0, students - persons.get(level + TEACH_DIFF - 1) * 10);
					teachers -= persons.get(level + TEACH_DIFF - 1);
				}
			}
		}
	}

	protected void increase(ArrayList<Integer> arrayList, Integer key, int inc) {
		for (int i = arrayList.size(); i <= key; ++i) {
			arrayList.add(0);
		}
		arrayList.set(key, arrayList.get(key) + inc);
	}

	@SuppressWarnings("unused")
	protected void increase(Map<Integer, Integer> map, Integer key, int inc) {
		Integer old = map.get(key);
		if (old == null) {
			map.put(key, inc);
		} else {
			map.put(key, old + inc);
		}
	}

	public double getMinGlobalWeight() {
		return minGlobalWeight;
	}

	public void setMinGlobalWeight(double weight) {
		minGlobalWeight = weight;
	}

}
