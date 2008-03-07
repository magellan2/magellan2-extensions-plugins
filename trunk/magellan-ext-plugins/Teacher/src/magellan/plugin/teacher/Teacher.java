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

	private UserInterface ui = new NullUserInterface();

	Teacher(Collection<Unit> units, String namespace, UserInterface ui) {
		this.units = units;
		this.namespace = namespace;
		this.ui = ui;
	}

	private SUnit[] sUnits;
	public static final String delim = " ";

	Random random = new Random();

	/**
	 * Represents a unit with teaching and learning preferences.
	 * 
	 * @author stm
	 * 
	 */
	public static class SUnit {

		Unit unit;
		Map<String, Integer> teach = new HashMap<String, Integer>();
		Map<String, Double> learn = new HashMap<String, Double>();
		private int index;
		private ArrayList<Integer> teachers = new ArrayList<Integer>();

		SUnit(Unit unit) {
			this.unit = unit;
		}

		public void addTeach(String talent, Integer prio) {
			teach.put(talent, prio);
		}

		public void addLearn(String talent, Double prio) {
			learn.put(talent, prio);
		}

		public Unit getUnit() {
			return unit;
		}

		public double getPrio(String learning) {
			if (learning == null)
				return 0;
			return (Double) (learn.get(learning) != null ? learn.get(learning) : -1);
		}

		public int getMaximumDifference(String teaching) {
			if (teaching == null)
				return 1;
			return (teach.get(teaching) != null ? teach.get(teaching) : 1);
		}

		public Set<String> getLearnTalents() {
			return learn.keySet();
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

		public void setTags() {
			Unit u = getUnit();
			double maxPrio = Double.NEGATIVE_INFINITY;
			String maxLearning = null;
			String maxTeaching = null;
			for (String t : getLearnTalents()) {
				if (getPrio(t) > maxPrio) {
					maxPrio = getPrio(t);
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
	}

	/**
	 * Represents a solution to the teaching problem.
	 * 
	 * @author steffen
	 */
	class Solution implements Comparable<Solution> {

		/**
		 * Represents what a unit is doing in a solution (teaching or learning etc.)
		 * 
		 * @author steffen
		 */
		class Info {
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
		}

		private static final double LEVEL_VALUE = .2;
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
					value = su.getPrio(info.learning);
					value *= su.getUnit().getModifiedPersons();
					value *= 1 + LEVEL_VALUE * getLevel(su.getUnit(), info.learning);
					if (info.getNumTeachers() > 0) {
						Info teacher = infos[info.teacher];
						int sLevel = getLevel(info.getUnit(), info.learning);
						int tLevel = getLevel(teacher.getUnit(), info.learning);
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
					|| getLevel(teacher.getUnit(), student.learning) < getLevel(student.getUnit(),
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

			if (student.getPrio(talent) > 0 && diff != 1
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
	 * @return A {@link SUnit} according to <code>u</code>'s orders, <code>null</code> if this
	 *         unit has no teaching or learning orders
	 */
	public static SUnit parseUnit(Unit u, String namespace, boolean setTags) {
		SUnit su = null;
		boolean errorFlag = false;
		for (String orderString : u.getOrders()) {
			try {
				List<Order> orders = parseOrder(orderString, getTeachTag(namespace), getLearnTag(namespace));
				for (Order order : orders) {
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
						double prio = order.getValue();
						if (su == null)
							su = new SUnit(u);
						if (talent.equals(Order.ALL)) {
							double prio2 = order.getLowValue();

							if (u.getModifiedSkills().isEmpty())
								continue;

							// find min an max skill
							int maxSkill = 1, minSkill = Integer.MAX_VALUE;
							for (Skill s : u.getModifiedSkills()) {
								int l = s.getLevel();
								if (l > maxSkill)
									maxSkill = l;
								if (l < minSkill)
									minSkill = l;
							}
							// add all skills weighted by their level
							for (Skill s : u.getModifiedSkills()) {
								su.addLearn(s.getName(), s.getLevel() / (double) maxSkill * (prio - prio2) + prio2);
							}
						} else {
							su.addLearn(talent, prio);
						}
					}
				}
			} catch (Exception e) {
				log.warn(e + " parse error, unit " + u + " line " + orderString);
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
			su.setTags();
		return su;
	}

	protected static List<Order> parseOrder(String orderString, String teachTag, String learnTag) {
		List<Order> result = new ArrayList<Order>(4);

		int start = orderString.indexOf(teachTag);
		if (start != -1) {
			StringTokenizer st = new java.util.StringTokenizer(orderString.substring(start
					+ learnTag.length()), delim, false);
			while (st.hasMoreElements()) {
				String talent = st.nextToken();
				int diff = Integer.parseInt(st.nextToken());
				result.add(new Order(talent, diff));
			}
		}
		start = orderString.indexOf(learnTag);
		if (start != -1) {
			java.util.StringTokenizer st = new java.util.StringTokenizer(orderString.substring(start
					+ teachTag.length()), delim, false);
			while (st.hasMoreElements()) {
				String talent = st.nextToken();
				double prio = Double.parseDouble(st.nextToken());
				if (Order.ALL.equals(talent)) {
					double prio2 = Double.parseDouble(st.nextToken());
					result.add(new Order(prio, prio2));
				} else {
					result.add(new Order(talent, prio));
				}
			}
		}

		return result;
	}

	/**
	 * Parses all units and sets tags.
	 * 
	 */
	public void parse() {
		for (Unit u : units) {
			SUnit su = parseUnit(u, namespace, true);
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
		final int maxRounds = Math.max(120, (int) (sUnits.length) * 8);
		final int popSize = minRounds * 3 / 2;
		final int numMetaRounds = 3;
		final int select = 5;

		ui.setTitle("");
		ui.setMaximum(numMetaRounds * maxRounds + maxRounds);
		ui.show();

		Solution[] best = new Solution[numMetaRounds * select * 3 / 2];
		init(best, sUnits);

		for (int metaRound = 0; metaRound < numMetaRounds; ++metaRound) {
			Solution[] population = new Solution[popSize];
			init(population, sUnits);
			select(population);

			double oldBest = Double.NEGATIVE_INFINITY;
			int improved = 0;
			for (int round = 0; round < minRounds || (round < maxRounds && improved <= minRounds / 10); ++round) {
				if (population[0].evaluate() > oldBest)
					improved = 0;
				else
					improved++;
				oldBest = population[0].evaluate();
				recombine(population);
				if (round % Math.ceil(minRounds / 10) == 0) {
					log.info(round + " 0: " + population[0].evaluate() + " " + population.length / 10 + ": "
							+ population[population.length / 10].evaluate() + " " + (population.length / 10 * 9)
							+ " " + population[population.length / 10 * 9].evaluate() + " "
							+ (population.length - 1) + ": " + population[population.length - 1].evaluate());
					ui.setProgress((metaRound > 0 ? best[metaRound - 1].evaluate() : "0") + " - "
							+ population[0].evaluate(), metaRound * maxRounds + round);
					mutate(population, Math.min(1 / Math.log(round + 1), .2));
				} else
					mutate(population, Math.min(.3 / Math.log(round + 1), .2));

				select(population);
			}
			log.info("***" + minRounds + " 0: " + population[0].evaluate() + " " + population.length / 10
					+ ": " + population[population.length / 10].evaluate() + " "
					+ (population.length / 10 * 9) + " " + population[population.length / 10 * 9].evaluate()
					+ " " + (population.length - 1) + ": " + population[population.length - 1].evaluate());
			for (int i = 0; i < select; ++i) {
				best[metaRound * select + i] = population[i];
			}
		}

		select(best);
		log.info(" 0: " + best[0].evaluate() + " l/3: " + best[best.length / 3].evaluate() + " "
				+ (best.length - 1) + ": " + best[best.length - 1].evaluate());
		for (int round = 0; round < minRounds * 2; ++round) {
			ui.setProgress("" + best[0].evaluate(), numMetaRounds * maxRounds + round);
			select(best);
			recombine(best);
			mutate(best, .1);
		}
		select(best);
		best[0].assignTeachers();
		log.info("***** 0: " + best[0].evaluate() + " l/3: " + best[best.length / 3].evaluate() + " "
				+ (best.length - 1) + ": " + best[best.length - 1].evaluate());
		ui.ready();

		if (best.length == 0)
			return -1;
		return setResult(best[0]);
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

	private void mutate(Solution[] population, double d) {
		// keep first individual?
		for (int i = 1; i < population.length / 2; ++i) {
			population[i].mutate(d);
		}

	}

	private void recombine(Solution[] population) {
		int n = population.length / 2;
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
				String o = (String) it.next();
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
				if (info.getTeacher() != -1 || info.students == info.getUnit().getModifiedPersons() * 10)
					info.getUnit().setOrdersConfirmed(true);
				else
					info.getUnit().setOrdersConfirmed(false);
			}
		}

		return best.evaluate();
	}

	public static void teach(final Collection<Unit> units, String namespace, final UserInterface ui) {
		(new Teacher(units, namespace, ui)).mainrun();

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
		// add new L order to all units
		for (Unit u : units) {
			Collection<String> oldOrders = u.getOrders();
			List<List<Order>> relevantOrders = new ArrayList<List<Order>>();
			List<String> newOrders = new ArrayList<String>(oldOrders.size());
			boolean foundSame = false;

			// look for L order
			for (String line : oldOrders){
				boolean isRelevant = false;
				List<Order> orderList = parseOrder(line, getTeachTag(namespace), getLearnTag(namespace));
				for (Order order : orderList) {
					if (order.getType().equals(newOrder.getType())) {
						if (!isRelevant)
							relevantOrders.add(orderList);
						isRelevant = true;
						if (order.getTalent().equals(newOrder.getTalent()))
							foundSame = true;
					}
				}
				if (!isRelevant) {
					newOrders.add(line);
				}
			}
			boolean added = false;
			if (relevantOrders.isEmpty()) {
				newOrders.add("// "
						+ (newOrder.getType().equals(Order.LEARN) ? getLearnTag(namespace)
								: getTeachTag(namespace)) + " " + newOrder.shortOrder());
			} else {
				for (List<Order> orderList : relevantOrders) {
					StringBuffer line = new StringBuffer("// ");
					line.append((newOrder.getType().equals(Order.LEARN) ? getLearnTag(namespace)
							: getTeachTag(namespace)));
					for (Order order : orderList) {
						line.append(" ");
						if (order.getTalent().equals(newOrder.getTalent())) {
							line.append(newOrder.shortOrder());
						} else {
							line.append(order.shortOrder());
						}
					}
					if (!added && !foundSame) {
						line.append(" ");
						line.append(newOrder.shortOrder());
						added = true;
					}
					newOrders.add(line.toString());
				}
			}
			u.setOrders(newOrders);
		}
	}

	public static void delOrder(Collection<Unit> units, String namespace, Order newOrder) {
		if (namespace == null)
			namespace = "";
		// add new L order to all units
		for (Unit u : units) {
			Collection<String> oldOrders = u.getOrders();
			List<String> newOrders = new ArrayList<String>(oldOrders.size());

			// look for L order
			for (String line : oldOrders){

				List<Order> orderList = parseOrder(line, getTeachTag(namespace), getLearnTag(namespace));
				if (orderList.isEmpty()) {
					newOrders.add(line);
				} else {
					List<Order> newOrderList = new ArrayList<Order>(orderList.size());
					for (Order order : orderList) {
						if (!order.getType().equals(newOrder.getType())
								|| !order.getTalent().equals(newOrder.getTalent())) {
							newOrderList.add(order);
						}
					}
					StringBuffer newLine = new StringBuffer("// ");
					boolean first = true;
					for (Order order : newOrderList) {
						if (first) {
							newLine.append((order.getType().equals(Order.LEARN) ? getLearnTag(namespace)
									: getTeachTag(namespace)));
							first = false;
						}
						newLine.append(" ");
						newLine.append(order.shortOrder());
					}
					if (!first) {
						newOrders.add(newLine.toString());
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

}
