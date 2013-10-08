package org.openlca.simapro.csv.model.types;

/**
 * Enumeration of SimaPro categories <<<<<<< Updated upstream =======
 * 
 * >>>>>>> Stashed changes
 */
public enum ProcessCategory {

	/**
	 * Material process
	 */
	MATERIAL("Material"),

	/**
	 * Energy process
	 */
	ENERGY("Energy"),

	/**
	 * Transport process
	 */
	TRANSPORT("Transport"),

	/**
	 * Processing process
	 */
	PROCESSING("Processing"),

	/**
	 * Use process
	 */
	USE("Use"),

	/**
	 * Transport process
	 */
	WASTE_SCENARIO("Waste scenario"),

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
			if (values()[i].getValue().toLowerCase()
					.equals(value.toLowerCase())) {
				category = values()[i];
			} else {
				i++;
			}
		}
		return category;
	}

}
