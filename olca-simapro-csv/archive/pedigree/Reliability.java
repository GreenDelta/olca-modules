package org.openlca.simapro.csv.model.enums.pedigree;

public enum Reliability {

	NA("na", "Unspecified", 1),

	ONE("1", "Verified data based on measurements", 1),

	TWO("2", "Verified data partly based on assumptions or non-verified data " +
			"on measurements", 1.05),

	THREE("3", "Non-verified data partly based on qualifired estimates", 1.1),

	FOUR("4", "Qualified estimate (e.g. by industrial expert)", 1.2),

	FIVE("5", "Non-qualified estimate", 1.5);

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

	private Reliability(String key, String value, double indicator) {
		this.key = key;
		this.value = value;
		this.indicator = indicator;
	}
}
