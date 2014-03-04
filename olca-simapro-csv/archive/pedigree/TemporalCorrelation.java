package org.openlca.simapro.csv.model.enums.pedigree;

public enum TemporalCorrelation {

	NA("na", "Unspecified", 1),

	ONE("1", "Less than 3 years of difference to the time period of the dataset"
			, 1),

	TWO("2", "Less than 6 years of difference to the time period of the dataset",
			1.03),

	THREE("3", "Less than 10 years of difference to the time period of the dataset",
			1.1),

	FOUR("4", "Less than 15 years of difference to the time period of the dataset",
			1.2),

	FIVE("5", "Age of data unknown or more than 15 years of difference to the " +
			"time period of the dataset", 1.5);

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
