
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for StandardMethodTypeValues.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="StandardMethodTypeValues">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="CML 2002"/>
 *     &lt;enumeration value="Eco-indicator 99"/>
 *     &lt;enumeration value="LIME 2004"/>
 *     &lt;enumeration value="EDIP 97"/>
 *     &lt;enumeration value="EDIP 2003"/>
 *     &lt;enumeration value="EPS 2000"/>
 *     &lt;enumeration value="LUCAS 2005 "/>
 *     &lt;enumeration value="TRACI 2000"/>
 *     &lt;enumeration value="TRACI 2008"/>
 *     &lt;enumeration value="ReCiPe 2007"/>
 *     &lt;enumeration value="IMPACT 2002+"/>
 *     &lt;enumeration value="MeEuP 2005"/>
 *     &lt;enumeration value="Ecoscarcity 2006"/>
 *     &lt;enumeration value="Other"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "StandardMethodTypeValues")
@XmlEnum
public enum StandardLciaMethod {

    @XmlEnumValue("CML 2002")
    CML_2002("CML 2002"),
    @XmlEnumValue("Eco-indicator 99")
    ECO_INDICATOR_99("Eco-indicator 99"),
    @XmlEnumValue("LIME 2004")
    LIME_2004("LIME 2004"),
    @XmlEnumValue("EDIP 97")
    EDIP_97("EDIP 97"),
    @XmlEnumValue("EDIP 2003")
    EDIP_2003("EDIP 2003"),
    @XmlEnumValue("EPS 2000")
    EPS_2000("EPS 2000"),
    @XmlEnumValue("LUCAS 2005 ")
    LUCAS_2005("LUCAS 2005 "),
    @XmlEnumValue("TRACI 2000")
    TRACI_2000("TRACI 2000"),
    @XmlEnumValue("TRACI 2008")
    TRACI_2008("TRACI 2008"),
    @XmlEnumValue("ReCiPe 2007")
    RE_CI_PE_2007("ReCiPe 2007"),
    @XmlEnumValue("IMPACT 2002+")
    IMPACT_2002("IMPACT 2002+"),
    @XmlEnumValue("MeEuP 2005")
    ME_EU_P_2005("MeEuP 2005"),
    @XmlEnumValue("Ecoscarcity 2006")
    ECOSCARCITY_2006("Ecoscarcity 2006"),
    @XmlEnumValue("Other")
    OTHER("Other");
    private final String value;

    StandardLciaMethod(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static StandardLciaMethod fromValue(String v) {
        for (StandardLciaMethod c: StandardLciaMethod.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
