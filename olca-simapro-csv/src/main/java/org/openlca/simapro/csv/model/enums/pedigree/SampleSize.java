package org.openlca.simapro.csv.model.enums.pedigree;

public enum SampleSize {

	NA("na", "Unspecified", 1),

	ONE("1", ">100, continuous measurement, balance of purchased product", 1),

	TWO("2", ">20", 1.02),

	THREE("3", "> 10, aggregated figure in env. report", 1.05),

	FOUR("4", ">=3", 1.1),

	FIVE("5", "unknown", 1.2);

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
