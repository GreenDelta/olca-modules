package org.openlca.simapro.csv.model.pedigreetypes;

public enum TemporalCorrelation {

	/**
	 * na
	 */
	NA("na", "Unspecified", 1),

	/**
	 * one
	 */
	ONE(
			"1",
			"Less than 3 years of difference to the time period of the dataset",
			1),

	/**
	 * two
	 */
	TWO(
			"2",
			"Less than 6 years of difference to the time period of the dataset",
			1.03),

	/**
	 * three
	 */
	THREE(
			"3",
			"Less than 10 years of difference to the time period of the dataset",
			1.1),
	/**
	 * four
	 */
	FOUR(
			"4",
			"Less than 15 years of difference to the time period of the dataset",
			1.2),
	/**
	 * five
	 */
	FIVE(
			"5",
			"Age of data unknown or more than 15 years of difference to the time period of the dataset",
			1.5);

	private String key;
	private String value;
	private double indicator;

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public double getIndicator() {
		return indicator;
	}

	private TemporalCorrelation(String key, String value, double indicator) {
		this.key = key;
		this.value = value;
		this.indicator = indicator;
	}
}
