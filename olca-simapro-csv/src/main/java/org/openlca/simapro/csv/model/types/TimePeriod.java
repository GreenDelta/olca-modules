package org.openlca.simapro.csv.model.types;

/**
 * Enumeration of possible time periods
 */
public enum TimePeriod {

	/**
	 * Mixed data (several time periods)
	 */
	MIXED_DATA("Mixed data"),

	/**
	 * Time period 1980 - 1984
	 */
	P_1980_1984("1980-1984"),

	/**
	 * Time period ? - 1980
	 */
	P_1980_AND_BEFORE("Before 1980"),

	/**
	 * Time period 1985 - 1989
	 */
	P_1985_1989("1985-1989"),

	/**
	 * Time period 1990 - 1994
	 */
	P_1990_1994("1990-1994"),

	/**
	 * Time period 1995 - 1999
	 */
	P_1995_1999("1995-1999"),

	/**
	 * Time period 2000 - 2004
	 */
	P_2000_2004("2000-2004"),

	/**
	 * Time period 2005 - 2009
	 */
	P_2005_2009("2005-2009"),

	/**
	 * Time period 2010 - ?
	 */
	P_2010_AND_AFTER("2010 and after"),

	/**
	 * Unknown time period
	 */
	UNKNOWN("Unknown"),

	/**
	 * Unspecified time period
	 */
	UNSPECIFIED("Unspecified");

	private String value;

	private TimePeriod(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static TimePeriod forValue(String value) {
		TimePeriod timePeriod = null;
		int i = 0;
		while (timePeriod == null && i < values().length) {
			if (values()[i].getValue().equals(value)) {
				timePeriod = values()[i];
			} else {
				i++;
			}
		}
		return timePeriod;
	}

}
