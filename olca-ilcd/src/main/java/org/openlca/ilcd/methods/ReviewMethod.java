
package org.openlca.ilcd.methods;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MethodOfReviewValues.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MethodOfReviewValues">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Recollection / Validation of data"/>
 *     &lt;enumeration value="Recalculation"/>
 *     &lt;enumeration value="Cross-check with other source"/>
 *     &lt;enumeration value="Cross-check with other LCIA method(ology)"/>
 *     &lt;enumeration value="Expert judgement"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MethodOfReviewValues")
@XmlEnum
public enum ReviewMethod {

    @XmlEnumValue("Recollection / Validation of data")
    RECOLLECTION_VALIDATION_OF_DATA("Recollection / Validation of data"),
    @XmlEnumValue("Recalculation")
    RECALCULATION("Recalculation"),
    @XmlEnumValue("Cross-check with other source")
    CROSS_CHECK_WITH_OTHER_SOURCE("Cross-check with other source"),
    @XmlEnumValue("Cross-check with other LCIA method(ology)")
    CROSS_CHECK_WITH_OTHER_LCIA_METHOD_OLOGY("Cross-check with other LCIA method(ology)"),
    @XmlEnumValue("Expert judgement")
    EXPERT_JUDGEMENT("Expert judgement");
    private final String value;

    ReviewMethod(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ReviewMethod fromValue(String v) {
        for (ReviewMethod c: ReviewMethod.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
