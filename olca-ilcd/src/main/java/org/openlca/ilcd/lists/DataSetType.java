
package org.openlca.ilcd.lists;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DataSetType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DataSetType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Process"/>
 *     &lt;enumeration value="LCIAMethod"/>
 *     &lt;enumeration value="Flow"/>
 *     &lt;enumeration value="FlowProperty"/>
 *     &lt;enumeration value="UnitGroup"/>
 *     &lt;enumeration value="Source"/>
 *     &lt;enumeration value="Contact"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DataSetType")
@XmlEnum
public enum DataSetType {

    @XmlEnumValue("Process")
    PROCESS("Process"),
    @XmlEnumValue("LCIAMethod")
    LCIA_METHOD("LCIAMethod"),
    @XmlEnumValue("Flow")
    FLOW("Flow"),
    @XmlEnumValue("FlowProperty")
    FLOW_PROPERTY("FlowProperty"),
    @XmlEnumValue("UnitGroup")
    UNIT_GROUP("UnitGroup"),
    @XmlEnumValue("Source")
    SOURCE("Source"),
    @XmlEnumValue("Contact")
    CONTACT("Contact");
    private final String value;

    DataSetType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DataSetType fromValue(String v) {
        for (DataSetType c: DataSetType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
