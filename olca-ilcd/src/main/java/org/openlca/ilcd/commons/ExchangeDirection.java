
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ExchangeDirectionValues.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ExchangeDirectionValues">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Input"/>
 *     &lt;enumeration value="Output"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ExchangeDirectionValues")
@XmlEnum
public enum ExchangeDirection {


    /**
     * Flow in input list of the process, e.g. resources from nature or energy carriers, or commodities and services entering from the technosphere.
     *                         (In case the flow has an negative "resulting amount" value this is equivalent to belonging to the output list of the process.)
     * 
     */
    @XmlEnumValue("Input")
    INPUT("Input"),

    /**
     * Flow in output list of the process, e.g. emissions to nature, or products and waste going to the technosphere into another process. (In case
     *                         the flow has a negative "resulting amount" value this is equivalent to belonging to the input list of the process.)
     * 
     */
    @XmlEnumValue("Output")
    OUTPUT("Output");
    private final String value;

    ExchangeDirection(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ExchangeDirection fromValue(String v) {
        for (ExchangeDirection c: ExchangeDirection.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
