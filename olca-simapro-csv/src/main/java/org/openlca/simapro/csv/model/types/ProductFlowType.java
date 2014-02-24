package org.openlca.simapro.csv.model.types;

public enum ProductFlowType {

	AVOIDED_PRODUCT("Avoided product"),

	ELECTRICITY_INPUT("Electricity/heat"),

	MATERIAL_INPUT("Material/fuels"),

	WASTE_TREATMENT("Waste treatment");

	private String value;

	private ProductFlowType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ProductFlowType forValue(String value) {
		for (ProductFlowType type : values())
			if (type.getValue().equals(value))
				return type;
		return null;
	}

}
