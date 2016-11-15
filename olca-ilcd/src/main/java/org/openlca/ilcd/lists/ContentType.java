
package org.openlca.ilcd.lists;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Specifies the type of data sets that can be contained in a category.
 */
@XmlEnum
@XmlType(name = "DataSetType")
public enum ContentType {

	@XmlEnumValue("Process")
	PROCESS("Process"),

	@XmlEnumValue("LCIAMethod")
	LCIA_METHOD("LCIAMethod"),

	@XmlEnumValue("Flow")
	FLOW("Flow"),

	@XmlEnumValue("FlowProperty")
	FLOW_PROPERTY("FlowProperty"),

	@XmlEnumValue("UnitGroup")
	UNIT_GROUP("UnitGroup"),

	@XmlEnumValue("Source")
	SOURCE("Source"),

	@XmlEnumValue("Contact")
	CONTACT("Contact");

	private final String value;

	ContentType(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static ContentType fromValue(String v) {
		for (ContentType c : ContentType.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
