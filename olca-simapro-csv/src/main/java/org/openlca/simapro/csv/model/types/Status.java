package org.openlca.simapro.csv.model.types;

/**
 * Enumeration of possible status of a process
 * 
 * @author Sebastian Greve
 * 
 */
public enum Status {

	/**
	 * Status 'draft'
	 */
	DRAFT("Draft"),

	/**
	 * Status 'finished'
	 */
	FINISHED("Finished"),

	/**
	 * Status 'temporary'
	 */
	TEMPORARY("Temporary"),

	/**
	 * Status 'to be reviewed'
	 */
	TO_BE_REVIEWED("To be reviewed"),

	/**
	 * Status 'to be revised'
	 */
	TO_BE_REVISED("To be revised");

	private String value;

	private Status(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Status forValue(String value) {
		Status status = null;
		int i = 0;
		while (status == null && i < values().length) {
			if (values()[i].getValue().equals(value)) {
				status = values()[i];
			} else {
				i++;
			}
		}
		return status;
	}

}
