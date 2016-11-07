
package org.openlca.ilcd.methods;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "LCIAMethodPrincipleValues")
@XmlEnum
public enum LCIAMethodPrinciple {

	@XmlEnumValue("Distance-to-target")
	DISTANCE_TO_TARGET("Distance-to-target"),

	@XmlEnumValue("Critical surface-time")
	CRITICAL_SURFACE_TIME("Critical surface-time"),

	@XmlEnumValue("Effective volumes")
	EFFECTIVE_VOLUMES("Effective volumes"),

	@XmlEnumValue("AoP-Damage model")
	AO_P_DAMAGE_MODEL("AoP-Damage model"),

	@XmlEnumValue("Carrying capacity")
	CARRYING_CAPACITY("Carrying capacity"),

	@XmlEnumValue("Resource dissipation")
	RESOURCE_DISSIPATION("Resource dissipation"),

	@XmlEnumValue("other")
	OTHER("other");

	private final String value;

	LCIAMethodPrinciple(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static LCIAMethodPrinciple fromValue(String v) {
		for (LCIAMethodPrinciple c : LCIAMethodPrinciple.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
