package org.openlca.simapro.csv.model.types;

/**
 * Enumeration of possible substitution allocation
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public enum Substitution {

	/**
	 * Actual substitution
	 */
	ACTUAL_SUBSTITUTION("Actual substitution"),

	/**
	 * No substitution applicable
	 */
	NOT_APPLICABLE("Not applicable"),

	/**
	 * Substitution by close proxy (similar process)
	 */
	SUBSTITUTION_BY_CLOSE_PROXY("Substitution by close proxy (similar process)"),

	/**
	 * Substitution by distant proxy (different process)
	 */
	SUBSTITUTION_BY_DISTANT_PROXY(
			"Substitution by distant proxy (different process)"),

	/**
	 * Unknown substitution
	 */
	UNKNOWN("Unknown"),

	/**
	 * Unspecified substitution
	 */
	UNSPECIFIED("Unspecified");

	private String value;

	private Substitution(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Substitution forValue(String value) {
		Substitution substitution = null;
		int i = 0;
		while (substitution == null && i < values().length) {
			if (values()[i].getValue().equals(value)) {
				substitution = values()[i];
			} else {
				i++;
			}
		}
		return substitution;
	}
}
