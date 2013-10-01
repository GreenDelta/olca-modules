package org.openlca.simapro.csv.model.types;

/**
 * Enumeration of SimaPro categories
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public enum ProcessCategory {

	/**
	 * Energy process
	 */
	ENERGY("Energy"),

	/**
	 * Material process
	 */
	MATERIAL("Material"),

	/**
	 * Processing process
	 */
	PROCESSING("Processing"),

	/**
	 * Transport process
	 */
	TRANSPORT("Transport"),

	/**
	 * Use process
	 */
	USE("Use"),

	/**
	 * Waste treatment process
	 */
	WASTE_TREATMENT("Waste treatment");

	private String value;

	private ProcessCategory(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ProcessCategory forValue(String value) {
		ProcessCategory category = null;
		int i = 0;
		while (category == null && i < values().length) {
			if (values()[i].getValue().toLowerCase().equals(value.toLowerCase())) {
				category = values()[i];
			} else {
				i++;
			}
		}
		return category;
	}

}
