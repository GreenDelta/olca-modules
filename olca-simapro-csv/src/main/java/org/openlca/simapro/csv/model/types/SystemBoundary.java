package org.openlca.simapro.csv.model.types;

/**
 * Enumeration of system boundaries
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public enum SystemBoundary {

	/**
	 * Only primary flows
	 */
	FIRST_ORDER("First order (only primary flows)"),

	/**
	 * Material/energy flows including operations
	 */
	SECOND_ORDER("Second order (material/energy flows including operations)"),

	/**
	 * Including capital goods
	 */
	THIRD_ORDER("Third order (including capital goods)"),

	/**
	 * Unknown system boundary
	 */
	UNKNOWN("Unknown"),

	/**
	 * Unspecified system boundary
	 */
	UNSPECIFIED("Unspecified");

	private String value;

	private SystemBoundary(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static SystemBoundary forValue(String value) {
		SystemBoundary boundary = null;
		int i = 0;
		while (boundary == null && i < values().length) {
			if (values()[i].getValue().equals(value)) {
				boundary = values()[i];
			} else {
				i++;
			}
		}
		return boundary;
	}

}
