package org.openlca.simapro.csv.model.enums;

public enum Representativeness implements ValueEnum {

	AVERAGE_FROM_A_SPECIFIC_PROCESS("Average from a specific process"),

	AVERAGE_FROM_PROCESSES_WITH_SIMILAR_OUTPUTS(
			"Average from processes with similar outputs"),

	AVERAGE_OF_ALL_SUPPLIERS("Average of all suppliers"),

	DATA_BASED_ON_INPUT_OUTPUT_TABLES("Data based on input-output tables"),

	DATA_FROM_A_SPECIFIC_PROCESS_AND_COMPANY(
			"Data from a specific process and company"),

	ESTIMATE("Estimate"),

	MIXED_DATA("Mixed data"),

	THEORETICAL_CALCULATION("Theoretical calculation"),

	UNKNOWN("Unknown"),

	UNSPECIFIED("Unspecified");

	private String value;

	private Representativeness(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Representativeness forValue(String value) {
		for (Representativeness r : values())
			if (r.getValue().equals(value))
				return r;
		return UNSPECIFIED;
	}

}
