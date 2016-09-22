
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "MethodOfReviewValues")
@XmlEnum
public enum ReviewMethod {

	/**
	 * Analysis of all data sources declared, checking their context-specific
	 * correct use as well as their relevance and quality.
	 */
	@XmlEnumValue("Validation of data sources")
	VALIDATION_OF_DATA_SOURCES("Validation of data sources"),

	/**
	 * Values in the inventory are re-calculated from the raw data, or other
	 * calculations are validated exemplarily, e.g. scaling, averaging, summing
	 * up, stochiometric calculations, formulas in the mathematical models, etc.
	 */
	@XmlEnumValue("Sample tests on calculations")
	SAMPLE_TESTS_ON_CALCULATIONS("Sample tests on calculations"),

	/**
	 * The energy balance (e.g. gross or net calorific value or exergy) of the
	 * Inputs and Outputs is validated. [Note: For processes that have undergone
	 * allocation or consequential modeling the value of this review method is
	 * limited.]
	 */
	@XmlEnumValue("Energy balance")
	ENERGY_BALANCE("Energy balance"),

	/**
	 * The balance of the relevant chemical elements of the Inputs and Outputs
	 * is calculated and validated. The validated elements should be named in
	 * the review comments. [Note: For processes that have undergone allocation
	 * or consequential modeling the value of this review method is limited.]
	 */
	@XmlEnumValue("Element balance")
	ELEMENT_BALANCE("Element balance"),

	/**
	 * Comparison with other, independent data and/or information sources (can
	 * be both database and literature).
	 */
	@XmlEnumValue("Cross-check with other source")
	CROSS_CHECK_WITH_OTHER_SOURCE("Cross-check with other source"),

	/**
	 * Comparison with similar process or product system from the same or from
	 * other sources (can be both database and literature).
	 */
	@XmlEnumValue("Cross-check with other data set")
	CROSS_CHECK_WITH_OTHER_DATA_SET("Cross-check with other data set"),

	/**
	 * Analysis by means of expert opinions. The expert(s) have methodological
	 * and detailed technical expertise on the item to be verified and the
	 * process or product system in question.
	 */
	@XmlEnumValue("Expert judgement")
	EXPERT_JUDGEMENT("Expert judgement"),

	/**
	 * The mass balance of the Inputs and Outputs is validated. [Note: For
	 * processes that have undergone allocation or consequential modeling the
	 * value of this review method is limited.]
	 */
	@XmlEnumValue("Mass balance")
	MASS_BALANCE("Mass balance"),

	/**
	 * Regulated Inputs and Outputs e.g. emission data are validated for
	 * compliance with legal limits, typically after relating and scaling the
	 * data to the regulated processes/sites etc.
	 */
	@XmlEnumValue("Compliance with legal limits")
	COMPLIANCE_WITH_LEGAL_LIMITS("Compliance with legal limits"),

	/**
	 * Methodological compliance with ISO 14040 to 14044 was checked by an LCA
	 * expert.
	 */
	@XmlEnumValue("Compliance with ISO 14040 to 14044")
	COMPLIANCE_WITH_ISO_14040_TO_14044("Compliance with ISO 14040 to 14044"),

	/**
	 * Evidence collection by means of documentation (e.g. data set's meta data,
	 * background report)
	 */
	@XmlEnumValue("Documentation")
	DOCUMENTATION("Documentation"),

	/**
	 * Interviews and/or plant visits are performed to validate data and other
	 * informtion, tyically in case of inconsistencies, uncertainties, or
	 * doubts. People interviewed have detailed technical expertise on the
	 * analysed process(es).
	 */
	@XmlEnumValue("Evidence collection by means of plant visits and/or interviews")
	EVIDENCE_COLLECTION_BY_MEANS_OF_PLANT_VISITS_AND_OR_INTERVIEWS("Evidence collection by means of plant visits and/or interviews");

	private final String value;

	ReviewMethod(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static ReviewMethod fromValue(String v) {
		for (ReviewMethod c : ReviewMethod.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
