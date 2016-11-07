
package org.openlca.ilcd.methods;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "AreaOfProtectionValues")
@XmlEnum
public enum AreaOfProtection {

	@XmlEnumValue("Natural resources") 
	NATURAL_RESOURCES("Natural resources"),

	@XmlEnumValue("Natural environment") 
	NATURAL_ENVIRONMENT("Natural environment"),

	@XmlEnumValue("Human health") 
	HUMAN_HEALTH("Human health"),

	@XmlEnumValue("Man-made environment") 
	MAN_MADE_ENVIRONMENT("Man-made environment"),

	@XmlEnumValue("Other") 
	OTHER("Other");

	private final String value;

	AreaOfProtection(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static AreaOfProtection fromValue(String v) {
		for (AreaOfProtection c : AreaOfProtection.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
