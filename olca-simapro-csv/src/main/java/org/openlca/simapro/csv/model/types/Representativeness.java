package org.openlca.simapro.csv.model.types;

/**
 * Enumeration of possible representativeness values
 * 
 * @author Sebastian Greve
 * 
 */
public enum Representativeness {

	/**
	 * Average data for a specific process
	 */
	AVERAGE_FROM_A_SPECIFIC_PROCESS("Average from a specific process"),

	/**
	 * Average data for processes with similar outputs
	 */
	AVERAGE_FROM_PROCESSES_WITH_SIMILAR_OUTPUTS(
			"Average from processes with similar outputs"),

	/**
	 * Average data for all suppliers of the product
	 */
	AVERAGE_OF_ALL_SUPPLIERS("Average of all suppliers"),

	/**
	 * Data based on input output tables
	 */
	DATA_BASED_ON_INPUT_OUTPUT_TABLES("Data based on input-output tables"),

	/**
	 * Data is only representative for a specific process and company
	 */
	DATA_FROM_A_SPECIFIC_PROCESS_AND_COMPANY(
			"Data from a specific process and company"),

	/**
	 * Estimated data
	 */
	ESTIMATE("Estimate"),

	/**
	 * The representativeness is mixed
	 */
	MIXED_DATA("Mixed data"),

	/**
	 * Theoratical calculation
	 */
	THEORETICAL_CALCULATION("Theoretical calculation"),

	/**
	 * The representativeness is unknown
	 */
	UNKNOWN("Unknown"),

	/**
	 * The representativeness is unspecified
	 */
	UNSPECIFIED("Unspecified");

	private String value;

	private Representativeness(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Representativeness forValue(String value) {
		Representativeness representativeness = null;
		int i = 0;
		while (representativeness == null && i < values().length) {
			if (values()[i].getValue().equals(value)) {
				representativeness = values()[i];
			} else {
				i++;
			}
		}
		return representativeness;
	}

}
