package org.openlca.simapro.csv.model.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Besitzer
 * 
 */
public enum ProductFlowType {

	PRODUCTS("Products"),

	AVOIDED_PRODUCTS("Avoided products"),

	ELECTRICITY_HEAT("Electricity/heat"),

	MATERIAL_FUELS("Materials/fuels"),

	WASTE_TO_TREATMENT("Waste to treatment");

	private final String header;

	private ProductFlowType(String header) {
		this.header = header;
	}

	public String getValue() {
		return value;
	}

	public static ProductFlowType forValue(String value) {
		for (ProductFlowType type : values())
			if (type.getValue().equalsIgnoreCase(value))
				return type;
		Logger log = LoggerFactory.getLogger(value);
		log.warn("unknown product type {}; returning NULL", value);
		return null;
	}

}
