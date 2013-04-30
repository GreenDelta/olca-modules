
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TypeOfProcessValues.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="TypeOfProcessValues">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Unit process, single operation"/>
 *     &lt;enumeration value="Unit process, black box"/>
 *     &lt;enumeration value="LCI result"/>
 *     &lt;enumeration value="Partly terminated system"/>
 *     &lt;enumeration value="Avoided product system"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "TypeOfProcessValues")
@XmlEnum
public enum ProcessType {


    /**
     * Unit operation type unit process that can not be further subdivided. Covers multi-functional processes of unit operation
     *                         type.
     * 
     */
    @XmlEnumValue("Unit process, single operation")
    UNIT_PROCESS_SINGLE_OPERATION("Unit process, single operation"),

    /**
     * Process-chain or plant level unit process. This covers horizontally averaged unit processes across different sites. Covers also those
     *                         multi-functional unit processes, where the different co-products undergo different processing steps within the black box, hence causing allocation-problems
     *                         for this data set.
     * 
     */
    @XmlEnumValue("Unit process, black box")
    UNIT_PROCESS_BLACK_BOX("Unit process, black box"),

    /**
     * Aggregated data set of the complete or partial life cycle of a product system that next to the elementary flows (and possibly not relevant
     *                         amounts of waste flows and radioactive wastes) lists in the input/output list exclusively the product(s) of the process as reference flow(s), but no other
     *                         goods or services. E.g. cradle-to-gate and cradle-to-grave data sets. Check also the definition of "Partly terminated system".
     * 
     */
    @XmlEnumValue("LCI result")
    LCI_RESULT("LCI result"),

    /**
     * Aggregated data set with however at least one product flow in the input/output list that needs further modelling, in addition to the reference
     *                         flow(s). E.g. a process of an injection moulding machine with one open "Electricity" input product flow that requires the LCA practitioner to saturate with
     *                         an Electricity production LCI data set (e.g. of the country where the machine is operated). Note that also aggregated process data sets that include
     *                         relevant amounts of waste flows for which the waste management has not been modelled yet are "partly terminated system" data sets.
     * 
     */
    @XmlEnumValue("Partly terminated system")
    PARTLY_TERMINATED_SYSTEM("Partly terminated system"),

    /**
     * Data set with all flows set to negative values OR all inputs be made to outputs and vice versa; i.e. a negative/inverted inventory (can be
     *                         unit process, LCI result, or other type). Used in system expansion/substitution for consequential modelling.
     * 
     */
    @XmlEnumValue("Avoided product system")
    AVOIDED_PRODUCT_SYSTEM("Avoided product system");
    private final String value;

    ProcessType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ProcessType fromValue(String v) {
        for (ProcessType c: ProcessType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
