
package org.openlca.ilcd.processes;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "MethodOfReviewValues")
@XmlEnum
public enum ReviewMethod {

	@XmlEnumValue("Validation of data sources")
	VALIDATION_OF_DATA_SOURCES("Validation of data sources"),
	@XmlEnumValue("Sample tests on calculations")
	SAMPLE_TESTS_ON_CALCULATIONS("Sample tests on calculations"),
	@XmlEnumValue("Energy balance")
	ENERGY_BALANCE("Energy balance"),
	@XmlEnumValue("Element balance")
	ELEMENT_BALANCE("Element balance"),
	@XmlEnumValue("Cross-check with other source")
	CROSS_CHECK_WITH_OTHER_SOURCE("Cross-check with other source"),
	@XmlEnumValue("Cross-check with other data set")
	CROSS_CHECK_WITH_OTHER_DATA_SET("Cross-check with other data set"),
	@XmlEnumValue("Expert judgement")
	EXPERT_JUDGEMENT("Expert judgement"),
	@XmlEnumValue("Mass balance")
	MASS_BALANCE("Mass balance"),
	@XmlEnumValue("Compliance with legal limits")
	COMPLIANCE_WITH_LEGAL_LIMITS("Compliance with legal limits"),
	@XmlEnumValue("Compliance with ISO 14040 to 14044")
	COMPLIANCE_WITH_ISO_14040_TO_14044("Compliance with ISO 14040 to 14044"),
	@XmlEnumValue("Documentation")
	DOCUMENTATION("Documentation"),
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
