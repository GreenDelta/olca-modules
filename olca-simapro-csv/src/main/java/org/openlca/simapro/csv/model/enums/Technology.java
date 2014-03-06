package org.openlca.simapro.csv.model.enums;

public enum Technology implements ValueEnum {

	AVERAGE_TECHNOLOGY("Average technology"),

	BEST_AVAILABLE_TECHNOLOGY("Best available technology"),

	FUTURE_TECHNOLOGY("Future technology"),

	MIXED_DATA("Mixed data"),

	MODERN_TECHNOLOGY("Modern technology"),

	OUTDATED_TECHNOLOGY("Outdated technology"),

	UNKNOWN("Unknown"),

	UNSPECIFIED("Unspecified"),

	WORST_CASE("Worst case");

	private String value;

	private Technology(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Technology forValue(String value) {
		for (Technology technology : values())
			if (technology.getValue().equals(value))
				return technology;
		return UNSPECIFIED;
	}

}
