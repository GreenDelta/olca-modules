package org.openlca.sd.xmile;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum XmiGfType {

	@XmlEnumValue("continuous")
	CONTINUOUS("continuous"),

	@XmlEnumValue("extrapolate")
	EXTRAPOLATE("extrapolate"),

	@XmlEnumValue("discrete")
	DISCRETE("discrete");

	private final String value;

	XmiGfType(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	public static XmiGfType fromValue(String value) {
		for (XmiGfType type : XmiGfType.values()) {
			if (type.value.equals(value)) {
				return type;
			}
		}
		return null;
	}
}
