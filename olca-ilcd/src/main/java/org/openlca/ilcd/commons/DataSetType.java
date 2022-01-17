
package org.openlca.ilcd.commons;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "GlobalReferenceTypeValues")
@XmlEnum
public enum DataSetType {

	@XmlEnumValue("source data set")
	SOURCE("source data set"),

	@XmlEnumValue("process data set")
	PROCESS("process data set"),

	@XmlEnumValue("flow data set")
	FLOW("flow data set"),

	@XmlEnumValue("flow property data set")
	FLOW_PROPERTY("flow property data set"),

	@XmlEnumValue("unit group data set")
	UNIT_GROUP("unit group data set"),

	@XmlEnumValue("contact data set")
	CONTACT("contact data set"),

	@XmlEnumValue("LCIA method data set")
	LCIA_METHOD("LCIA method data set"),

	@XmlEnumValue("other external file")
	EXTERNAL_FILE("other external file"),

	@XmlEnumValue("life cycle model")
	MODEL("life cycle model");

	private final String value;

	DataSetType(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static DataSetType fromValue(String v) {
		for (var type : DataSetType.values()) {
			if (type.value.equals(v)) {
				return type;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return value;
	}
}
