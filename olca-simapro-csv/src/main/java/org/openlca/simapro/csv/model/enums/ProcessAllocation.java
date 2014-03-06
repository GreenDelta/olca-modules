package org.openlca.simapro.csv.model.enums;

public enum ProcessAllocation implements ValueEnum {

	NOT_APPLICABLE("Not applicable"),

	PHYSICAL_CAUSALITY("Physical causality"),

	SOCIO_ECONOMIC_CAUSALITY("Socio-economic causality"),

	UNKNOWN("Unknown"),

	UNSPECIFIED("Unspecified");

	private String value;

	private ProcessAllocation(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ProcessAllocation forValue(String value) {
		for (ProcessAllocation allocation : values())
			if (allocation.getValue().equals(value))
				return allocation;
		return UNSPECIFIED;
	}

}
