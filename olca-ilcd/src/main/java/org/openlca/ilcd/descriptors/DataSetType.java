
package org.openlca.ilcd.descriptors;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GlobalReferenceTypeValues.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="GlobalReferenceTypeValues">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="source data set"/>
 *     &lt;enumeration value="process data set"/>
 *     &lt;enumeration value="flow data set"/>
 *     &lt;enumeration value="flow property data set"/>
 *     &lt;enumeration value="unit group data set"/>
 *     &lt;enumeration value="contact data set"/>
 *     &lt;enumeration value="LCIA method data set"/>
 *     &lt;enumeration value="other external file"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "GlobalReferenceTypeValues", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
@XmlEnum
public enum DataSetType {

    @XmlEnumValue("source data set")
    SOURCE_DATA_SET("source data set"),
    @XmlEnumValue("process data set")
    PROCESS_DATA_SET("process data set"),
    @XmlEnumValue("flow data set")
    FLOW_DATA_SET("flow data set"),
    @XmlEnumValue("flow property data set")
    FLOW_PROPERTY_DATA_SET("flow property data set"),
    @XmlEnumValue("unit group data set")
    UNIT_GROUP_DATA_SET("unit group data set"),
    @XmlEnumValue("contact data set")
    CONTACT_DATA_SET("contact data set"),
    @XmlEnumValue("LCIA method data set")
    LCIA_METHOD_DATA_SET("LCIA method data set"),
    @XmlEnumValue("other external file")
    OTHER_EXTERNAL_FILE("other external file");
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
