package org.openlca.simapro.csv.model.enums;

public enum Substitution implements ValueEnum {

	ACTUAL_SUBSTITUTION("Actual substitution"),

	NOT_APPLICABLE("Not applicable"),

	SUBSTITUTION_BY_CLOSE_PROXY("Substitution by close proxy (similar process)"),

	SUBSTITUTION_BY_DISTANT_PROXY(
			"Substitution by distant proxy (different process)"),

	UNKNOWN("Unknown"),

	UNSPECIFIED("Unspecified");

	private String value;

	private Substitution(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Substitution forValue(String value) {
		for (Substitution substitution : values())
			if (substitution.getValue().equals(value))
				return substitution;
		return UNSPECIFIED;
	}
}
