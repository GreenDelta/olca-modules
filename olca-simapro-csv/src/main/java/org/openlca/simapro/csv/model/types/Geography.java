package org.openlca.simapro.csv.model.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of possible locations
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public enum Geography {

	/**
	 * Africa
	 */
	AFRICA("Africa"),

	/**
	 * Arctic regions
	 */
	ARCTIC_REGIONS("Arctic regions"),

	/**
	 * China
	 */
	ASIA_CHINA("Asia, China"),

	/**
	 * Asia (Former USSR)
	 */
	ASIA_FORMER_USSR("Asia, former USSR"),

	/**
	 * Indian region
	 */
	ASIA_INDIAN_REGION("Asia, Indian region"),

	/**
	 * Japan
	 */
	ASIA_JAPAN("Asia, Japan"),

	/**
	 * Korea
	 */
	ASIA_KOREA("Asia, Korea"),

	/**
	 * Middle east asia
	 */
	ASIA_MIDDLE_EAST("Asia, Middle East"),

	/**
	 * South east asia
	 */
	ASIA_SOUTH_EAST("Asia, South East"),

	/**
	 * Australia
	 */
	AUSTRALIA("Australia"),

	/**
	 * Eastern europe
	 */
	EUROPE_EASTERN("Europe, Eastern"),

	/**
	 * Western europe
	 */
	EUROPE_WESTERN("Europe, Western"),

	/**
	 * Several different locations
	 */
	MIXED_DATA("Mixed data"),

	/**
	 * North America
	 */
	NORTH_AMERICA("North America"),

	/**
	 * Oceans
	 */
	OCEANS("Oceans"),

	/**
	 * South and central America
	 */
	SOUTH_AND_CENTRAL_AMERICA("South and Central America"),

	/**
	 * Unknown location
	 */
	UNKNOWN("Unknown"),

	/**
	 * Unspecified location
	 */
	UNSPECIFIED("Unspecified"),

	/**
	 * Whole world
	 */
	WORLD("World");

	private String value;

	private Geography(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Geography forValue(String value) {
		Geography geography = null;
		int i = 0;
		while (geography == null && i < values().length) {
			if (values()[i].getValue().equals(value)) {
				geography = values()[i];
			} else {
				i++;
			}
		}
		return geography;
	}

	private static final Map<String, Geography> instances;

	static {
		Map<String, Geography> lInstances = new HashMap<String, Geography>();

		for (Geography eft : Geography.values()) {
			lInstances.put(eft.value, eft);
		}
		instances = Collections.unmodifiableMap(lInstances);
	}

	public static Map<String, Geography> getInstances() {
		return instances;
	}
}
