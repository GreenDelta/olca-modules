package org.openlca.simapro.csv.model.enums;

public enum BoundaryWithNature implements ValueEnum {

	AGRICULTURAL_NATURAL_SYSTEM(
			"Agricultural production is part of natural systems"),

	AGRICULTURAL_PRODUCTION_SYSTEM(
			"Agricultural production is part of production system"),

	NOT_APPLICABLE("Not applicable"),

	UNKNOWN("Unknown"),

	UNSPECIFIED("Unspecified");

	private String value;

	private BoundaryWithNature(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static BoundaryWithNature forValue(String value) {
		for (BoundaryWithNature v : values()) {
			if (v.getValue().equals(value))
				return v;
		}
		return UNSPECIFIED;
	}

}
