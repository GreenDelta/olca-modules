
package org.openlca.ilcd.lists;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "DataSetType")
@XmlEnum
public enum DataSetType {

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

	DataSetType(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static DataSetType fromValue(String v) {
		for (DataSetType c : DataSetType.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
