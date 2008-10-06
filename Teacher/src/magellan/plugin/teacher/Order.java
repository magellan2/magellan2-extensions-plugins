/**
 * 
 */
package magellan.plugin.teacher;

public class Order {
	public static final String LEARN = "L";
	public static final String TEACH = "T";
	public static final String ALL = "ALLES";

	private String type;
	private String talent;
	
	private int diff;
	
	private double prio;
	private int target;
	private int max;

	/**
	 * Create a new teaching order.
	 * 
	 * @param talent
	 * @param diff
	 */
	public Order(String talent, int diff, boolean sec) {
		this.type = TEACH;
		this.talent = talent.trim();
		this.diff = diff;
	}

	/**
	 * Create a new learning order
	 * 
	 * @param talent
	 * @param value
	 * @throws IllegalArgumentException if target is <= 0
	 */
	public Order(String talent, double prio,  int target, int max) {
		if (target<=0)
			throw new IllegalArgumentException("target must be > 0");
		this.type = LEARN;
		this.prio = prio;
		this.talent = talent.trim();
		this.target = target;
		this.max = max;
	}

	// /**
	// * Create a new learn ALLES order.
	// *
	// * @param upper
	// * @param lower
	// */
	// public Order(double upper, double lower) {
	// this.type = LEARN;
	// this.talent = ALL;
	// this.value = upper;
	// this.lowValue = lower;
	// }

	/**
	 * Create a new teach ALLES order.
	 * 
	 * @param diff
	 */
	public Order(int diff) {
		this.type = TEACH;
		this.talent = ALL;
		this.diff = diff;
	}

	/**
	 * Returns the type of order
	 * 
	 * @return {@link #LEARN} for learn orders, {@link #TEACH} for teaching orders
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return The name of the talent.
	 */
	public String getTalent() {
		return talent;
	}

	/**
	 * The priority of a learn order.
	 * 
	 * @return 
	 * @throws IllegalArgumentException
	 *           if this is called for a {@link #TEACH} order.
	 */
	public double getPrio() {
		if (TEACH.equals(type))
			throw new IllegalArgumentException("TEACH has no priority");
		return prio;
	}

	/**
	 * Change priority of learn order.
	 * 
	 * @param prio2
	 * @throws IllegalArgumentException
	 *           if this is called for a {@link #TEACH} order.
	 */
	public void setPrio(double prio) {
		if (TEACH.equals(type))
			throw new IllegalArgumentException("TEACH has no priority");
		this.prio = prio;
	}

	/**
	 * The target value of a learn order.
	 * 
	 * @return
	 * @throws IllegalArgumentException
	 *           if this is called for a {@link #TEACH} order.
	 */
	public int getTarget() {
		if (TEACH.equals(type))
			throw new IllegalArgumentException("TEACH has no target");
		return target;
	}

	/**
	 * The value of a learn order.
	 * 
	 * @return
	 * @throws IllegalArgumentException
	 *           if this is called for a {@link #TEACH} order.
	 */
	public int getMax() {
		if (TEACH.equals(type))
			throw new IllegalArgumentException("TEACH has no max");
		return max;
	}

	/**
	 * The difference of a teach order.
	 * 
	 * @return
	 * @throws IllegalArgumentException
	 *           if this is called from a {@link #LEARN}.
	 */
	public int getDiff() {
		if (LEARN.equals(type))
			throw new IllegalArgumentException("LEARN has no diff");
		return diff;
	}

	/**
	 * @return Something like "Talent value"
	 */
	public String shortOrder() {
		if (LEARN.equals(type))
			if (ALL.equals(talent))
				return "ALLERROR"; // talent + " " + value + " " + lowValue;
			else
				return talent + " " + target + " " + max;
		else
			return talent + " " + diff;
	}

	/**
	 * @return Something like "L Talent value"
	 */
	public String longOrder() {
		if (LEARN.equals(getType()))
			return getType() + " " + getPrio() + " " + shortOrder();
		else
			return getType() + " " + shortOrder();
	}

	public String toString() {
		return longOrder();
	}

	public boolean isTeachOrder() {
		return getType().equals(TEACH);
	}

	public boolean isLearnOrder() {
		return getType().equals(LEARN);
	}

}