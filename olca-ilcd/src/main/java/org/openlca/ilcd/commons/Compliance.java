
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ComplianceValues.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ComplianceValues">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Fully compliant"/>
 *     &lt;enumeration value="Not compliant"/>
 *     &lt;enumeration value="Not defined"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ComplianceValues")
@XmlEnum
public enum Compliance {


    /**
     * Meets all requirements of this compliance aspect as defined in the respective "Compliance system".
     * 
     */
    @XmlEnumValue("Fully compliant")
    FULLY_COMPLIANT("Fully compliant"),

    /**
     * Does not meet all requirements of this compliance aspect, as defined for the respective "Compliance system".
     * 
     */
    @XmlEnumValue("Not compliant")
    NOT_COMPLIANT("Not compliant"),

    /**
     * For this compliance aspect the named "Compliance system" has not defined compliance requirements.
     * 
     */
    @XmlEnumValue("Not defined")
    NOT_DEFINED("Not defined");
    private final String value;

    Compliance(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Compliance fromValue(String v) {
        for (Compliance c: Compliance.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
