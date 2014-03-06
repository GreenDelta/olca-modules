package org.openlca.simapro.csv.model.enums;

public enum TimePeriod implements ValueEnum {

	MIXED_DATA("Mixed data"),

	P_1980_1984("1980-1984"),

	P_1980_AND_BEFORE("Before 1980"),

	P_1985_1989("1985-1989"),

	P_1990_1994("1990-1994"),

	P_1995_1999("1995-1999"),

	P_2000_2004("2000-2004"),

	P_2005_2009("2005-2009"),

	P_2010_AND_AFTER("2010 and after"),

	UNKNOWN("Unknown"),

	UNSPECIFIED("Unspecified");

	private String value;

	private TimePeriod(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}

	public static TimePeriod forValue(String value) {
		for (TimePeriod time : values())
			if (time.getValue().equals(value))
				return time;
		return UNSPECIFIED;
	}

}
