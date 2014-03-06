package org.openlca.simapro.csv.model.enums;

public enum CutOffRule implements ValueEnum {

	ENVIRONMENTAL_RELEVANCE_LESS_THAN_1(
			"Less than 1% (environmental relevance)"),

	ENVIRONMENTAL_RELEVANCE_LESS_THAN_5(
			"Less than 5% (environmental relevance)"),

	NOT_APPLICABLE("Not applicable"),

	PHYSICAL_LESS_THAN_1("Less than 1% (physical criteria)"),

	PHYSICAL_LESS_THAN_5("Less than 5% (physical criteria)"),

	SOCIO_ECONOMIC_LESS_THAN_1("Less than 1% (socio economic)"),

	SOCIO_ECONOMIC_LESS_THAN_5("Less than 5% (socio economic)"),

	UNKNOWN("Unknown"),

	UNSPECIFIED("Unspecified");

	private String value;

	private CutOffRule(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static CutOffRule forValue(String value) {
		for (CutOffRule rule : values()) {
			if (rule.getValue().equals(value))
				return rule;
		}
		return UNSPECIFIED;
	}
}
