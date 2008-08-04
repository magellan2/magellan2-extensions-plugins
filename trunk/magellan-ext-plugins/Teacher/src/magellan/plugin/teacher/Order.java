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
	private double value;
	private double lowValue = Double.NaN;

	/**
	 * Create a new teaching order.
	 * 
	 * @param talent
	 * @param diff
	 */
	public Order(String talent, int diff) {
		this.type = TEACH;
		this.talent = talent.trim();
		this.diff = diff;
	}

	/**
	 * Create a new learning order
	 * 
	 * @param talent
	 * @param value
	 */
	public Order(String talent, double value) {
		this.type = LEARN;
		this.talent = talent.trim();
		this.value = value;
	}

	/**
	 * Create a new learn ALLES order.
	 * 
	 * @param upper
	 * @param lower
	 */
	public Order(double upper, double lower) {
		this.type = LEARN;
		this.talent = ALL;
		this.value = upper;
		this.lowValue = lower;
	}

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
	 * The value of a learn order.
	 * 
	 * @return
	 * @throws IllegalArgumentException if this is called for a {@link #TEACH} order.
	 */
	public double getValue() {
		if (TEACH.equals(type))
			throw new IllegalArgumentException("TEACH has no value");
		return value;
	}

	/**
	 * The lower value of a learn ALL order.
	 * 
	 * @return
	 * @throws IllegalArgumentException if this is not called from a {@link #LEARN} {@link #ALL} order.
	 */
	public double getLowValue() {
		if (TEACH.equals(type) || !ALL.equals(talent))
			throw new IllegalArgumentException("no low value");
		return lowValue;
	}

	/**
	 * The difference of a teach order.
	 * 
	 * @return
	 * @throws IllegalArgumentException if this is called from a {@link #LEARN}.
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
				return talent + " " + value + " " + lowValue;
			else
				return talent + " " + value;
		else
			return talent + " " + diff;
	}

	/**
	 * @return Something like "L Talent value"
	 */
	public String longOrder() {
		return getType() + " " + shortOrder();
	}

	public String toString() {
		return longOrder();
	}
}