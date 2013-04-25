package org.openlca.simapro.csv.model.types;

/**
 * Enumeration of SimaPro process types
 * 
 * @author Sebastian Greve
 * 
 */
public enum ProcessType {

	/**
	 * System process
	 */
	SYSTEM("System"),

	/**
	 * Unit process
	 */
	UNIT_PROCESS("Unit process");

	private String value;

	private ProcessType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ProcessType forValue(String value) {
		ProcessType type = null;
		int i = 0;
		while (type == null && i < values().length) {
			if (values()[i].getValue().equals(value)) {
				type = values()[i];
			} else {
				i++;
			}
		}
		return type;
	}

}
