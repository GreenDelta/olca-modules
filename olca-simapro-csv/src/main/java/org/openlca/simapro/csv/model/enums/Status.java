package org.openlca.simapro.csv.model.enums;

public enum Status implements ValueEnum {

	DRAFT("Draft"),

	FINISHED("Finished"),

	TEMPORARY("Temporary"),

	TO_BE_REVIEWED("To be reviewed"),

	TO_BE_REVISED("To be revised");

	private String value;

	private Status(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Status forValue(String value) {
		for (Status status : values())
			if (status.getValue().equals(value))
				return status;
		return null;
	}

}
