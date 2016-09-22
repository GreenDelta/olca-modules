
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "TypeOfOrganisationValues")
@XmlEnum
public enum OrganisationType {

	/**
	 * Private company
	 * 
	 */
	@XmlEnumValue("Private company")
	PRIVATE_COMPANY("Private company"),

	/**
	 * Governmental organisation
	 * 
	 */
	@XmlEnumValue("Governmental")
	GOVERNMENTAL("Governmental"),

	/**
	 * Non-governmental organisation
	 * 
	 */
	@XmlEnumValue("Non-governmental org.")
	NON_GOVERNMENTAL_ORG("Non-governmental org."),

	/**
	 * Other, e.g. a project consortium or network
	 * 
	 */
	@XmlEnumValue("Other")
	OTHER("Other");

	private final String value;

	OrganisationType(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static OrganisationType fromValue(String v) {
		for (OrganisationType c : OrganisationType.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
