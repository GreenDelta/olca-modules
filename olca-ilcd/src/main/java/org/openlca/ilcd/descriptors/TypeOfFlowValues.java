
package org.openlca.ilcd.descriptors;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "TypeOfFlowValues", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
@XmlEnum
public enum TypeOfFlowValues {

	/**
	 * Exchange between nature (ecosphere) and technosphere, e.g. an emission,
	 * resource.
	 */
	@XmlEnumValue("Elementary flow")
	ELEMENTARY_FLOW("Elementary flow"),

	/**
	 * Exchange of goods or services within technosphere, with a positive
	 * economic/market value.
	 */
	@XmlEnumValue("Product flow")
	PRODUCT_FLOW("Product flow"),

	/**
	 * Exchange of matters within the technosphere, with a economic/market value
	 * equal or below "0".
	 */
	@XmlEnumValue("Waste flow")
	WASTE_FLOW("Waste flow"),

	/**
	 * Exchange of other type, e.g. dummy or modelling support flows.
	 */
	@XmlEnumValue("Other flow")
	OTHER_FLOW("Other flow");
	private final String value;

	TypeOfFlowValues(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static TypeOfFlowValues fromValue(String v) {
		for (TypeOfFlowValues c : TypeOfFlowValues.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
