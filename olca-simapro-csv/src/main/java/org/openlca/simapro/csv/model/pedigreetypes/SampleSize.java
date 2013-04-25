package org.openlca.simapro.csv.model.pedigreetypes;

public enum SampleSize {
	/**
	 * na
	 */
	NA("na", "Unspecified", 1),

	/**
	 * one
	 */
	ONE("1", ">100, continous measurement, balance of pruchased product", 1),

	/**
	 * two
	 */
	TWO("2", ">20", 1.02),

	/**
	 * three
	 */
	THREE("3", "> 10, aggrefated figure in env. report", 1.05),
	/**
	 * four
	 */
	FOUR("4", ">=3", 1.1),
	/**
	 * five
	 */
	FIVE("5", "uknown", 1.2);

	private String key;
	private String value;
	double indicator;

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public double getIndicator() {
		return indicator;
	}

	private SampleSize(String key, String value, double indicator) {
		this.key = key;
		this.value = value;
		this.indicator = indicator;
	}
}
