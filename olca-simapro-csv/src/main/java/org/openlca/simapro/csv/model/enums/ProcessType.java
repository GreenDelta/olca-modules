package org.openlca.simapro.csv.model.enums;

public enum ProcessType implements ValueEnum {

	SYSTEM("System"),

	UNIT_PROCESS("Unit process");

	private String value;

	private ProcessType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ProcessType forValue(String value) {
		for (ProcessType type : values())
			if (type.getValue().equals(value))
				return type;
		return null;
	}

}
