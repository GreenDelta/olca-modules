package org.openlca.simapro.csv.model.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of possible locations
 */
public enum Geography {

	/**
	 * Africa
	 */
	AFRICA("Africa", "35d21e4a-ab38-493b-8d96-89f73064b0d5"),

	/**
	 * Arctic regions
	 */
	ARCTIC_REGIONS("Arctic regions", "06f138a4-9920-4b13-adc9-3db56ca686e1"),

	/**
	 * China
	 */
	ASIA_CHINA("Asia, China", "4346af8b-8bed-40b8-a69d-9d9e24002a81"),

	/**
	 * Asia (Former USSR)
	 */
	ASIA_FORMER_USSR("Asia, former USSR",
			"f7896ba6-83a5-4d31-8152-0b253499cdde"),

	/**
	 * Indian region
	 */
	ASIA_INDIAN_REGION("Asia, Indian region",
			"6baa27e6-9da6-4de9-ac9e-3c37e2472a7e"),

	/**
	 * Japan
	 */
	ASIA_JAPAN("Asia, Japan", "1bf2eb37-2aac-401c-b164-bd96643f02d7"),

	/**
	 * Korea
	 */
	ASIA_KOREA("Asia, Korea", "70e76b8d-70a4-4959-b8ea-8a737ea1c905"),

	/**
	 * Middle east asia
	 */
	ASIA_MIDDLE_EAST("Asia, Middle East",
			"d6a74e32-e22a-44fa-b156-d3e7bb7e7b3b"),

	/**
	 * South east asia
	 */
	ASIA_SOUTH_EAST("Asia, South East", "ecc709b1-3130-468f-ac0f-128033248ca4"),

	/**
	 * Australia
	 */
	AUSTRALIA("Australia", "e8905d7b-a59b-4ac7-a16e-3201e2a64732"),

	/**
	 * Eastern europe
	 */
	EUROPE_EASTERN("Europe, Eastern", "ef36ca76-6f88-4876-aad2-b4b302ad4956"),

	/**
	 * Western europe
	 */
	EUROPE_WESTERN("Europe, Western", "e246ae90-8288-4cde-bdfb-191ec366230e"),

	/**
	 * Several different locations
	 */
	MIXED_DATA("Mixed data", "759da9a8-bd50-4603-8794-47c1d042b0ee"),

	/**
	 * North America
	 */
	NORTH_AMERICA("North America", "44511364-d30f-4036-a7d9-693fb9f272c1"),

	/**
	 * Oceans
	 */
	OCEANS("Oceans", "2564cdde-42d1-4a04-a0b0-f5fadc7660f6"),

	/**
	 * South and central America
	 */
	SOUTH_AND_CENTRAL_AMERICA("South and Central America",
			"907d890a-626f-4bb3-9ca9-7ba481f34572"),

	/**
	 * Unknown location
	 */
	UNKNOWN("Unknown", "abffdf35-818d-4232-b78b-3c6bdcd1acfa"),

	/**
	 * Unspecified location
	 */
	UNSPECIFIED("Unspecified", "661e052f-80e3-4502-b6cc-7f82c89391a4"),

	/**
	 * Whole world
	 */
	WORLD("World", "369e2806-4727-4d60-bd13-6e8df7ce8984");

	private String value;

	private String es2Id;

	private Geography(String value, String es2Id) {
		this.value = value;
		this.es2Id = es2Id;
	}

	public String getValue() {
		return value;
	}

	public String getES2Id() {
		return es2Id;
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
