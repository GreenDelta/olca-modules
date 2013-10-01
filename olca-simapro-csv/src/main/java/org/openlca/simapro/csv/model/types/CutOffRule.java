package org.openlca.simapro.csv.model.types;

/**
 * Enumeration of possible cut off rules
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public enum CutOffRule {

	/**
	 * Less than 1% (environmental relevance)
	 */
	ENVIRONMENTAL_RELEVANCE_LESS_THAN_1(
			"Less than 1% (environmental relevance)"),

	/**
	 * Less than 5% (environmental relevance)
	 */
	ENVIRONMENTAL_RELEVANCE_LESS_THAN_5(
			"Less than 5% (environmental relevance)"),

	/**
	 * No cut off applicable
	 */
	NOT_APPLICABLE("Not applicable"),

	/**
	 * Less than 1% (physical criteria)
	 */
	PHYSICAL_LESS_THAN_1("Less than 1% (physical criteria)"),

	/**
	 * Less than 5% (physical criteria)
	 */
	PHYSICAL_LESS_THAN_5("Less than 5% (physical criteria)"),

	/**
	 * Less than 1% (socio economic)
	 */
	SOCIO_ECONOMIC_LESS_THAN_1("Less than 1% (socio economic)"),

	/**
	 * Less than 5% (socio economic)
	 */
	SOCIO_ECONOMIC_LESS_THAN_5("Less than 5% (socio economic)"),

	/**
	 * Unknown cut off
	 */
	UNKNOWN("Unknown"),

	/**
	 * Unspecified cut off
	 */
	UNSPECIFIED("Unspecified");

	private String value;

	private CutOffRule(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static CutOffRule forValue(String value) {
		CutOffRule rule= null;
		int i = 0;
		while (rule== null && i < values().length) {
			if (values()[i].getValue().equals(value)) {
				rule= values()[i];
			} else {
				i++;
			}
		}
		return rule;
	}
}
