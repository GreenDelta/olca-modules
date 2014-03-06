package org.openlca.simapro.csv.model.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ProcessCategory implements ValueEnum {

	MATERIAL("material"),

	ENERGY("energy"),

	TRANSPORT("transport"),

	PROCESSING("processing"),

	USE("use"),

	WASTE_SCENARIO("waste scenario"),

	WASTE_TREATMENT("waste treatment");

	private String value;

	private ProcessCategory(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}

	public static ProcessCategory forValue(String value) {
		for (ProcessCategory category : values())
			if (category.getValue().equalsIgnoreCase((value)))
				return category;
		Logger log = LoggerFactory.getLogger(ProcessCategory.class);
		log.warn("unknown product category {}; returning NULL", value);
		return null;
	}
}
