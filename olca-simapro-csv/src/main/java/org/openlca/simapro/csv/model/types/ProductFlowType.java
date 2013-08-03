package org.openlca.simapro.csv.model.types;

/**
 * Enumeration of possible product flow types
 */
public enum ProductFlowType {

	/**
	 * Known outputs to technosphere, avoided products
	 */
	AVOIDED_PRODUCT("Avoided product"),

	/**
	 * Known inputs from technosphere (electricity/heat)
	 */
	ELECTRICITY_INPUT("Electricity/heat"),

	/**
	 * Known inputs from technosphere (materials/fuels)
	 */
	MATERIAL_INPUT("Material/fuels"),

	/**
	 * Known outputs to technosphere, Waste and emissions to treatment
	 */
	WASTE_TREATMENT("Waste treatment");

	private String value;

	private ProductFlowType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ProductFlowType forValue(String value) {
		ProductFlowType type = null;
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
