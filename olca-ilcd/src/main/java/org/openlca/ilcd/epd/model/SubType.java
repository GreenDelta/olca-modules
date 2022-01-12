package org.openlca.ilcd.epd.model;

public enum SubType {

	GENERIC("generic dataset"),

	REPRESENTATIVE("representative dataset"),

	AVERAGE("average dataset"),

	SPECIFIC("specific dataset"),

	TEMPLATE("template dataset");

	private final String label;

	SubType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static SubType fromLabel(String label) {
		if (label == null)
			return null;
		String l = label.trim();
		for (SubType type : values()) {
			if (type.label.equalsIgnoreCase(l))
				return type;
		}
		return null;
	}
}
