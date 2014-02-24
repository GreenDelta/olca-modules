package org.openlca.simapro.csv.model.enums;

public enum WasteTreatmentAllocation {

	CLOSED_LOOP_ASSUMPTION("Closed loop assumption"),

	FULL_SUBSTITUTION_BY_CLOSE_PROXY(
			"Full substitution by close proxy (similar process)"),

	FULL_SUBSTITUTION_BY_DISTANT_PROXY(
			"Full substitution by distant proxy (different process)"),

	NOT_APPLICABLE("Not applicable"),

	PARTIAL_SUBSTITUTION_PHYSICAL(
			"Partial substitution, physical basis for cut off"),

	PARTIAL_SUBSTITUTION_SOCIO_ECONOMIC(
			"Partial substitution, socio-economic basis for cut off"),

	UNKNOWN("Unknown"),

	UNSPECIFIED("Unspecified");

	private String value;

	private WasteTreatmentAllocation(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static WasteTreatmentAllocation forValue(String value) {
		for (WasteTreatmentAllocation allocation : values())
			if (allocation.getValue().equals(value))
				return allocation;
		return UNSPECIFIED;
	}

}
