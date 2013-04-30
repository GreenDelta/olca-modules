
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DataDerivationTypeStatusValues.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DataDerivationTypeStatusValues">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Measured"/>
 *     &lt;enumeration value="Calculated"/>
 *     &lt;enumeration value="Estimated"/>
 *     &lt;enumeration value="Unknown derivation"/>
 *     &lt;enumeration value="Missing important"/>
 *     &lt;enumeration value="Missing unimportant"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DataDerivationTypeStatusValues")
@XmlEnum
public enum DataDerivation {


    /**
     * All data was measured for the purpose of LCA or is meeting the related requirements (e.g. being quantiatively and qualitatively related to
     *                         unit process and its quantitative reference/products, etc.). This includes calculated data with models based on measured plant data (but not exclusively
     *                         theoretical calculations) as used e.g. in parameterised unit processes.
     * 
     */
    @XmlEnumValue("Measured")
    MEASURED("Measured"),

    /**
     * Stochiometric or other theoretical relations were used to calculate the amount of this flow. [Note: Calculations that include quantiatively
     *                         relevant expert judgement are of the type "Expert judgement".]
     * 
     */
    @XmlEnumValue("Calculated")
    CALCULATED("Calculated"),

    /**
     * Expert judgement was used to derive the quantity of this flow. This includes the unmodified or corrected use of data from similar processes /
     *                         technologies, times, or locations, as well as calculated values where the formulas/parameters include quantitatively relevant expert
     *                         judgement.
     * 
     */
    @XmlEnumValue("Estimated")
    ESTIMATED("Estimated"),

    /**
     * Data derivation type information fully or at least for quantiatively relevant parts unavailable.
     * 
     */
    @XmlEnumValue("Unknown derivation")
    UNKNOWN_DERIVATION("Unknown derivation"),

    /**
     * Indicates missing amount information for environmentally directly (elementary flow) or indirectly (product or waste flow) important Input or
     *                         Output. As the "Mean amount" the value "0" is entered.
     * 
     */
    @XmlEnumValue("Missing important")
    MISSING_IMPORTANT("Missing important"),

    /**
     * Indicates missing amount information for an however environmentally directly (elementary flow) or indirectly (product or waste flow) NOT
     *                         relevant Input or Output. As the "Mean amount" the value "0" is entered.
     * 
     */
    @XmlEnumValue("Missing unimportant")
    MISSING_UNIMPORTANT("Missing unimportant");
    private final String value;

    DataDerivation(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DataDerivation fromValue(String v) {
        for (DataDerivation c: DataDerivation.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
