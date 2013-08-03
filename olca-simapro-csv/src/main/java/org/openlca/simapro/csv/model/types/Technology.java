package org.openlca.simapro.csv.model.types;

/**
 * Enumeration of possible technologies
 */
public enum Technology {

	/**
	 * Average technology is used
	 */
	AVERAGE_TECHNOLOGY("Average technology"),

	/**
	 * The best available technology is used
	 */
	BEST_AVAILABLE_TECHNOLOGY("Best available technology"),

	/**
	 * Future technology is used
	 */
	FUTURE_TECHNOLOGY("Future technology"),

	/**
	 * Different technologies are used
	 */
	MIXED_DATA("Mixed data"),

	/**
	 * Modern technology is used
	 */
	MODERN_TECHNOLOGY("Modern technology"),

	/**
	 * Outdated technology is used
	 */
	OUTDATED_TECHNOLOGY("Outdated technology"),

	/**
	 * The technology is unknown
	 */
	UNKNOWN("Unknown"),

	/**
	 * The technology is not specified
	 */
	UNSPECIFIED("Unspecified"),

	/**
	 * The worst case technology is used
	 */
	WORST_CASE("Worst case");

	private String value;

	private Technology(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Technology forValue(String value) {
		Technology technology = null;
		int i = 0;
		while (technology == null && i < values().length) {
			if (values()[i].getValue().equals(value)) {
				technology = values()[i];
			} else {
				i++;
			}
		}
		return technology;
	}

}
