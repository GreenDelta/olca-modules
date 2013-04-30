package org.openlca.ilcd.productmodel;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ParameterScopeValues")
@XmlEnum
public enum ParameterScopeValues {
	@XmlEnumValue("global")
	GLOBAL("global"), 
	@XmlEnumValue("productModel")
	PRODUCTMODEL("productModel");

	private final String value;

	ParameterScopeValues(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static ParameterScopeValues fromValue(String v) {
		for (ParameterScopeValues c : ParameterScopeValues.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

	public String getValue() {
		return value;
	}

}
