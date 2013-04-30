
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AreaOfProtectionValues.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="AreaOfProtectionValues">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Natural resources"/>
 *     &lt;enumeration value="Natural environment"/>
 *     &lt;enumeration value="Human health"/>
 *     &lt;enumeration value="Man-made environment"/>
 *     &lt;enumeration value="Other"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
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
        for (AreaOfProtection c: AreaOfProtection.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
