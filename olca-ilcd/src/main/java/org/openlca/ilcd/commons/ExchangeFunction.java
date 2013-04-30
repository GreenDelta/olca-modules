
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ExchangeFunctionTypeValues.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ExchangeFunctionTypeValues">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="General reminder flow"/>
 *     &lt;enumeration value="Allocation reminder flow"/>
 *     &lt;enumeration value="System expansion reminder flow"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ExchangeFunctionTypeValues")
@XmlEnum
public enum ExchangeFunction {


    /**
     * Reminder flow on the amount of (selected) product and waste flows of included processes. Purely informative flow for additional reporting that
     *                         has already been fully considered in the inventory of the data set. Serves to document e.g. the total amount of hazardous waste generated over the life
     *                         cycle of a product system.
     * 
     */
    @XmlEnumValue("General reminder flow")
    GENERAL_REMINDER_FLOW("General reminder flow"),

    /**
     * Reminder flow on allocated co-products that have been excluded during allocation when calculating the LCI results. Purely informative flow for
     *                         additional reporting that has already been fully considered in the inventory of the data set.
     * 
     */
    @XmlEnumValue("Allocation reminder flow")
    ALLOCATION_REMINDER_FLOW("Allocation reminder flow"),

    /**
     * Reminder flow on excluded co-products that have been excluded in consequential modelling e.g. by system expansion / substitution when
     *                         calculating the LCI results.
     * 
     */
    @XmlEnumValue("System expansion reminder flow")
    SYSTEM_EXPANSION_REMINDER_FLOW("System expansion reminder flow");
    private final String value;

    ExchangeFunction(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ExchangeFunction fromValue(String v) {
        for (ExchangeFunction c: ExchangeFunction.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
