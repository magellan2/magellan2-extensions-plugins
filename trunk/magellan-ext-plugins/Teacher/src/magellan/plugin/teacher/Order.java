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

	public String getType() {
		return type;
	}

	public String getTalent() {
		return talent;
	}

	public double getValue() {
		if (TEACH.equals(type))
			throw new IllegalArgumentException("TEACH has no value");
		return value;
	}

	public double getLowValue() {
		if (TEACH.equals(type) || !ALL.equals(talent))
			throw new IllegalArgumentException("no low value");
		return lowValue;
	}

	public int getDiff() {
		if (LEARN.equals(type))
			throw new IllegalArgumentException("LEARN has no diff");
		return diff;
	}

	public Order(String talent, int diff) {
		this.type = TEACH;
		this.talent = talent;
		this.diff = diff;
	}

	public Order(String talent, double value) {
		this.type = LEARN;
		this.talent = talent;
		this.value = value;
	}

	public Order(double upper, double lower) {
		this.type = LEARN;
		this.talent = ALL;
		this.value = upper;
		this.lowValue = lower;
	}

	public Order(int diff) {
		this.type = TEACH;
		this.talent = ALL;
		this.diff = diff;
	}

	public String shortOrder() {
		if (LEARN.equals(type))
			if (ALL.equals(talent))
				return talent + " " + value + " " + lowValue;
			else
				return talent + " " + value;
		else
			return talent + " " + diff;
	}

	public String longOrder() {
		return getType() + " " + shortOrder();
	}

	public String toString() {
		return longOrder();
	}
}