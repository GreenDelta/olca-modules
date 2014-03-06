package org.openlca.simapro.csv.model.enums;

public enum Geography implements ValueEnum {

	AFRICA("Africa"),

	ARCTIC_REGIONS("Arctic regions"),

	ASIA_CHINA("Asia, China"),

	ASIA_FORMER_USSR("Asia, former USSR"),

	ASIA_INDIAN_REGION("Asia, Indian region"),

	ASIA_JAPAN("Asia, Japan"),

	ASIA_KOREA("Asia, Korea"),

	ASIA_MIDDLE_EAST("Asia, Middle East"),

	ASIA_SOUTH_EAST("Asia, South East"),

	AUSTRALIA("Australia"),

	EUROPE_EASTERN("Europe, Eastern"),

	EUROPE_WESTERN("Europe, Western"),

	MIXED_DATA("Mixed data"),

	NORTH_AMERICA("North America"),

	OCEANS("Oceans"),

	SOUTH_AND_CENTRAL_AMERICA("South and Central America"),

	UNKNOWN("Unknown"),

	UNSPECIFIED("Unspecified"),

	WORLD("World");

	private String value;

	private Geography(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Geography forValue(String value) {
		for (Geography geography : values()) {
			if (geography.getValue().equals(value))
				return geography;
		}
		return UNSPECIFIED;
	}

}
