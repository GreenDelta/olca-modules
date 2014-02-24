package org.openlca.simapro.csv.model.enums.pedigree;

public enum Completeness {

	NA("na", "Unspecified", 1),

	ONE("1", "Representative data from all sites relevant for the market " +
			"considered, over an adequate period to even out normal " +
			"fluctuations", 1),

	TWO("2", "Representative data from >50% of the sites relevant for the market " +
			"considered, over an adequate period to even out normal " +
			"fluctuations", 1.02),

	THREE("3", "Representative data from only some sites (<<50%) relevant for " +
			"the market considered OR >50% of sites but from shorter periods",
			1.05),

	FOUR("4", "Representative data from only one site relevant for the market " +
			"considered OR some sites but from shorter periods", 1.10),

	FIVE("5", "Representativeness unknown or data from a small number of sites " +
			"AND from shorter periods", 1.20);

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

	private Completeness(String key, String value, double indicator) {
		this.key = key;
		this.value = value;
		this.indicator = indicator;
	}
}
