
package org.openlca.ilcd.descriptors;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for QualityValues.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="QualityValues">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Very good"/>
 *     &lt;enumeration value="Good"/>
 *     &lt;enumeration value="Fair"/>
 *     &lt;enumeration value="Poor"/>
 *     &lt;enumeration value="Very poor"/>
 *     &lt;enumeration value="Not evaluated / unknown"/>
 *     &lt;enumeration value="Not applicable"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "QualityValues", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
@XmlEnum
public enum QualityValues {


    /**
     * Meets the criterium to a very high degree, having or no relevant need for improvement. This is to be judged in view of the criterium's contribution to
     *                         the data set's potential overall environmental impact and in comparison to an ideal situation.
     * 
     */
    @XmlEnumValue("Very good")
    VERY_GOOD("Very good"),

    /**
     * Meets the criterium to a high degree, having little yet significant need for improvement. This is to be judged in view of the criterium's contribution
     *                         to the data set's potential overall environmental impact and in comparison to an ideal situation.
     * 
     */
    @XmlEnumValue("Good")
    GOOD("Good"),

    /**
     * Meets the criterium to a still sufficient degree, while having the need for improvement. This is to be judged in view of the criterium's contribution to
     *                         the data set's potential overall environmental impact and in comparison to an ideal situation.
     * 
     */
    @XmlEnumValue("Fair")
    FAIR("Fair"),

    /**
     * Does not meet the criterium to a sufficient degree, having the need for relevant improvement. This is to be judged in view of the criterium's
     *                         contribution to the data set's potential overall environmental impact and in comparison to an ideal situation.
     * 
     */
    @XmlEnumValue("Poor")
    POOR("Poor"),

    /**
     * Does not at all meet the criterium, having the need for very substantial improvement. This is to be judged in view of the criterium's contribution to
     *                         the data set's potential overall environmental impact and in comparison to an ideal situation.
     * 
     */
    @XmlEnumValue("Very poor")
    VERY_POOR("Very poor"),

    /**
     * This criterium was not reviewed or its quality could not be verified.
     * 
     */
    @XmlEnumValue("Not evaluated / unknown")
    NOT_EVALUATED_UNKNOWN("Not evaluated / unknown"),

    /**
     * This criterium is not applicable to this data set, e.g. its geographical representative can not be evaluated as it is a location-unspecific technology
     *                         unit process.
     * 
     */
    @XmlEnumValue("Not applicable")
    NOT_APPLICABLE("Not applicable");
    private final String value;

    QualityValues(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static QualityValues fromValue(String v) {
        for (QualityValues c: QualityValues.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
