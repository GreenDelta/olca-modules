
package org.openlca.ilcd.methods;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "MethodOfReviewValues")
@XmlEnum
public enum ReviewMethod {

	@XmlEnumValue("Recollection / Validation of data")
	RECOLLECTION_VALIDATION_OF_DATA("Recollection / Validation of data"),

	@XmlEnumValue("Recalculation")
	RECALCULATION("Recalculation"),

	@XmlEnumValue("Cross-check with other source")
	CROSS_CHECK_WITH_OTHER_SOURCE("Cross-check with other source"),

	@XmlEnumValue("Cross-check with other LCIA method(ology)")
	CROSS_CHECK_WITH_OTHER_LCIA_METHOD_OLOGY("Cross-check with other LCIA method(ology)"),

	@XmlEnumValue("Expert judgement")
	EXPERT_JUDGEMENT("Expert judgement");

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
