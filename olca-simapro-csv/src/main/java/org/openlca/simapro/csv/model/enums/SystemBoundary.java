package org.openlca.simapro.csv.model.enums;

public enum SystemBoundary {

	FIRST_ORDER("First order (only primary flows)"),

	SECOND_ORDER("Second order (material/energy flows including operations)"),

	THIRD_ORDER("Third order (including capital goods)"),

	UNKNOWN("Unknown"),

	UNSPECIFIED("Unspecified");

	private String value;

	private SystemBoundary(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static SystemBoundary forValue(String value) {
		for (SystemBoundary boundary : values())
			if (boundary.getValue().equals(value))
				return boundary;
		return null;
	}

}
