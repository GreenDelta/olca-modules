package org.openlca.simapro.csv.model.types;

/**
 * Enumeration for possible multiple output allocation
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public enum ProcessAllocation {

	/**
	 * No allocation applicable
	 */
	NOT_APPLICABLE("Not applicable"),

	/**
	 * Physical causality allocation
	 */
	PHYSICAL_CAUSALITY("Physical causality"),

	/**
	 * Socio-economic causality allocation
	 */
	SOCIO_ECONOMIC_CAUSALITY("Socio-economic causality"),

	/**
	 * Unknown allocation
	 */
	UNKNOWN("Unknown"),

	/**
	 * Unspecified allocation
	 */
	UNSPECIFIED("Unspecified");

	private String value;

	private ProcessAllocation(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ProcessAllocation forValue(String value) {
		ProcessAllocation allocation = null;
		int i = 0;
		while (allocation == null && i < values().length) {
			if (values()[i].getValue().equals(value)) {
				allocation = values()[i];
			} else {
				i++;
			}
		}
		return allocation;
	}

}
