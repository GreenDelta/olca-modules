package org.openlca.simapro.csv.model.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allowed uncertainty distribution types in SimaPro.
 */
public enum DistributionType {

	LOG_NORMAL("Lognormal"),

	NORMAL("Normal"),

	TRIANGLE("Triangle"),

	UNIFORM("Uniform"),

	UNDEFINED("Undefined");

	private final String value;

	private DistributionType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static DistributionType fromValue(String value) {
		for (DistributionType type : values()) {
			if (type.value.equalsIgnoreCase(value))
				return type;
		}
		Logger log = LoggerFactory.getLogger(DistributionType.class);
		log.warn("unknown distribution type value {}, returning undifined",
				value);
		return UNDEFINED;
	}

}
