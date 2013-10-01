package org.openlca.simapro.csv.model.types;

/**
 * Enumeration of possible waste treatment allocation
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public enum WasteTreatmentAllocation {

	/**
	 * Closed loop assumption
	 */
	CLOSED_LOOP_ASSUMPTION("Closed loop assumption"),

	/**
	 * Full substitution by close proxy (similar process)
	 */
	FULL_SUBSTITUTION_BY_CLOSE_PROXY(
			"Full substitution by close proxy (similar process)"),

	/**
	 * Full substitution by distant proxy (different process)
	 */
	FULL_SUBSTITUTION_BY_DISTANT_PROXY(
			"Full substitution by distant proxy (different process)"),

	/**
	 * No allocation applicable
	 */
	NOT_APPLICABLE("Not applicable"),

	/**
	 * Partial substitution, physical basis for cut off
	 */
	PARTIAL_SUBSTITUTION_PHYSICAL(
			"Partial substitution, physical basis for cut off"),

	/**
	 * Partial substitution, socio-economic basis for cut off
	 */
	PARTIAL_SUBSTITUTION_SOCIO_ECONOMIC(
			"Partial substitution, socio-economic basis for cut off"),

	/**
	 * Unknown allocation
	 */
	UNKNOWN("Unknown"),

	/**
	 * Unspecified allocation
	 */
	UNSPECIFIED("Unspecified");

	private String value;

	private WasteTreatmentAllocation(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static WasteTreatmentAllocation forValue(String value) {
		WasteTreatmentAllocation allocation = null;
		int i = 0;
		while (allocation == null && i < values().length) {
			if (values()[i].getValue().equals(value)) {
				allocation = values()[i];
			} else {
				i++;
			}
		}
		return allocation;
	}

}
