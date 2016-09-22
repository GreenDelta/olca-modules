
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "CompletenessAvailabilityValues")
@XmlEnum
public enum Availability {

	/**
	 * For all referenced LCIA methods an impact factor is provided for this
	 * flow, in case this flow contributes to the respective mid-point problem
	 * field in an relevant way. The impact factor for the specific flow has to
	 * cover all effect chains and impacts addressed by the specific LCIA
	 * method.
	 * 
	 */
	@XmlEnumValue("Fully available")
	FULLY_AVAILABLE("Fully available"),

	/**
	 * In one or more of the referenced impact methods, no impact factors are
	 * provided for this flow, while the flow's relevant contribution to that
	 * mid-point aspect is scientifically acknowledged by state-of-the-art. Or
	 * for that specific flow not all effect chains and impacts that are
	 * addressed by on or more of the respective LCIA methods are covered also
	 * for this flow.
	 * 
	 */
	@XmlEnumValue("Partly available")
	PARTLY_AVAILABLE("Partly available"),

	/**
	 * For none of the referenced impact methods impact factors are provided for
	 * this flow, while by scientific state-of-the-art knowledge this flow
	 * contributes to a relevant degree to the named mid-point problem field.
	 * 
	 */
	@XmlEnumValue("Not available")
	NOT_AVAILABLE("Not available"),

	/**
	 * Flow is not relevant for the specific mid-point problem field. E.g. it is
	 * an elementary flow such as "Carbon dioxide emissions to air" which does
	 * not contribute to "Human toxicity" or it is a product flow.
	 * 
	 */
	@XmlEnumValue("Topic not relevant")
	TOPIC_NOT_RELEVANT("Topic not relevant"),

	/**
	 * Unclear, not stated.
	 * 
	 */
	@XmlEnumValue("No statement")
	NO_STATEMENT("No statement");
	private final String value;

	Availability(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static Availability fromValue(String v) {
		for (Availability c : Availability.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
