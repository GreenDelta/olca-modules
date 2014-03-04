package org.openlca.simapro.csv.model.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ElementaryFlowType {

	RESOURCES("Resources"),

	EMISSIONS_TO_AIR("Emissions to air"),

	EMISSIONS_TO_WATER("Emissions to water"),

	EMISSIONS_TO_SOIL("Emissions to soil"),

	FINAL_WASTE_FLOWS("Final waste flows"),

	NON_MATERIAL_EMISSIONS("Non material emissions"),

	SOCIAL_ISSUES("Social issues"),

	ECONOMIC_ISSUES("Economic issues");

	private String value;

	private ElementaryFlowType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ElementaryFlowType forValue(String value) {
		for (ElementaryFlowType type : values()) {
			if (type.getValue().equalsIgnoreCase(value))
				return type;
		}
		Logger log = LoggerFactory.getLogger(value);
		log.warn("unknown elementary flow type {}; returning NULL", value);
		return null;
	}

}
