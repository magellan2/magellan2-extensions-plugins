package magellan.plugin.teacher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import magellan.library.ID;
import magellan.library.Named;
import magellan.library.Unit;

/**
 * Represents a unit with teaching and learning preferences.
 * 
 * @author stm
 * 
 */
public class SUnit implements Named {

	/**
	 * 
	 */
	private final Teacher teacher;

	private Unit unit;

	private double prio = 1;

	// a talent / int map of differences
	private Map<Integer, Integer> teach = new HashMap<Integer, Integer>();
	// a talent / int map of target skill levels
	private Map<Integer, Integer> targets = new HashMap<Integer, Integer>();
	// a talent / int map if maximum skill levels
	private Map<Integer, Integer> maxs = new HashMap<Integer, Integer>();
	
	/** calculated priorities */
	private Map<Integer, Double> weights = new HashMap<Integer, Double>();

	// a talent / int map of current skill levels
	private Map<Integer, Integer> talentLevels = new HashMap<Integer, Integer>();

	private int index;
	private ArrayList<Integer> teachers = new ArrayList<Integer>();

	SUnit(Teacher teacher, Unit unit) {
		this.teacher = teacher;
		this.unit = unit;
		this.prio = 1;
	}

	public void addTeach(Integer talent, Integer diff) {
		teach.put(talent, diff);
		talentLevels.put(talent, this.teacher.getLevel(getUnit(), talent));
		weights.clear();
	}

	public void addLearn(Integer talent, Integer target, Integer max) {
		if (target == 0)
			throw new IllegalArgumentException("target must be > 0");
		targets.put(talent, target);
		maxs.put(talent, max);
		talentLevels.put(talent, this.teacher.getLevel(getUnit(), talent));
		weights.clear();
	}

	public Unit getUnit() {
		return unit;
	}

	public double calcWeight(Integer skill) {
		if (skill == null)
			return 0;
		Double prio = weights.get(skill);
		if (prio == null) {
			// calc max mult
			double maxMult = 0;
			for (Integer skill2 : getLearnTalents()) {
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
				for (Integer skill2 : getLearnTalents()) {
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

	public int getTarget(Integer skill) {
		if (skill == null)
			return 0;
		return (targets.get(skill) != null ? targets.get(skill) : -1);
	}

	public int getTarget(String skill) {
		return getTarget(teacher.getSkillIndex(skill));
	}
	
	public int getMax(Integer skill) {
		if (skill == null)
			return 0;
		return (maxs.get(skill) != null ? maxs.get(skill) : -1);
	}

	public int getMax(String skill) {
		return getMax(teacher.getSkillIndex(skill));
	}
	
	public double getPrio() {
		return prio;
	}

	public void setPrio(double prio) {
		this.prio = prio;
	}

	public int getMaximumDifference(Integer sill) {
		if (sill == null)
			return 1;
		return (teach.get(sill) != null ? teach.get(sill) : 1);
	}

	public int getMaximumDifference(String skill) {
		return getMaximumDifference(teacher.getSkillIndex(skill));
	}
	
	public Collection<Integer> getLearnTalents() {
		return targets.keySet();
	}

	public Collection<String> getLearnTalentsAsString() {
		Collection<String> result = new HashSet<String>();
		for (Integer t : getLearnTalents()){
			result.add(teacher.getSkillName(t));
		}
		return result;
	}

	public Collection<Integer> getTeachTalents() {
		return teach.keySet();
	}

	public Collection<String> getTeachTalentsAsString() {
		Collection<String> result = new HashSet<String>();
		for (Integer t : getTeachTalents()){
			result.add(teacher.getSkillName(t));
		}
		return result;
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
		Integer maxLearning = null;
		Integer maxTeaching = null;
		for (Integer t : getLearnTalents()) {
			if (getTarget(t) > maxTarget) {
				maxTarget = getTarget(t);
				maxLearning = t;
			}

		}
		int maxTalent = 0;
		for (Integer t : getTeachTalents()) {
			if (this.teacher.getLevel(u, t) > maxTalent) {
				maxTalent = this.teacher.getLevel(u, t);
				maxTeaching = t;
			}

		}
		if (maxLearning != null) {
			u.putTag(Teacher.LEARN_TAG, this.teacher.getSkillName(maxLearning));
		}
		if (maxTeaching != null) {
			u.putTag(Teacher.TEACH_TAG, this.teacher.getSkillName(maxTeaching));
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
	 * @deprecated now as efficient as getLevel?
	 */
	public int getSkillLevel(Integer skill) {
		if (talentLevels != null) {
			Integer result = talentLevels.get(skill);
			if (result != null) {
				return result;
			}
		}
		int level = this.teacher.getLevel(getUnit(), skill);
		talentLevels.put(skill, level);
		return level;
	}


}