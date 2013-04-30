
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FlowDataDerivationTypeStatusValues.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="FlowDataDerivationTypeStatusValues">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Measured"/>
 *     &lt;enumeration value="Calculated"/>
 *     &lt;enumeration value="Estimated"/>
 *     &lt;enumeration value="Unknown derivation"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "FlowDataDerivationTypeStatusValues")
@XmlEnum
public enum FlowDataDerivation {


    /**
     * Data was measured for the flow; includes data from publications with measured data.
     * 
     */
    @XmlEnumValue("Measured")
    MEASURED("Measured"),

    /**
     * Stochiometric, enthalpic or other theoretical methods were used to systematically calculate the value of this property from another
     *                         characteristic.
     * 
     */
    @XmlEnumValue("Calculated")
    CALCULATED("Calculated"),

    /**
     * Expert judgement including the direct or modified use of data from similar flows, or from other locations and times (e.g. for market prices of
     *                         product flows).
     * 
     */
    @XmlEnumValue("Estimated")
    ESTIMATED("Estimated"),

    /**
     * Data derivation type information fully or at least for quantiatively relevant parts unavailable.
     * 
     */
    @XmlEnumValue("Unknown derivation")
    UNKNOWN_DERIVATION("Unknown derivation");
    private final String value;

    FlowDataDerivation(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FlowDataDerivation fromValue(String v) {
        for (FlowDataDerivation c: FlowDataDerivation.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
