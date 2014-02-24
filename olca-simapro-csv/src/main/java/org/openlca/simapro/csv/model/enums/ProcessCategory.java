package org.openlca.simapro.csv.model.enums;

public enum ProcessCategory {

	MATERIAL("Material"),

	ENERGY("Energy"),

	TRANSPORT("Transport"),

	PROCESSING("Processing"),

	USE("Use"),

	WASTE_SCENARIO("Waste scenario"),

	WASTE_TREATMENT("Waste treatment");

	private String value;

	private ProcessCategory(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ProcessCategory forValue(String value) {
		for (ProcessCategory category : values())
			if (category.getValue().equals(value))
				return category;
		return null;
	}

}
