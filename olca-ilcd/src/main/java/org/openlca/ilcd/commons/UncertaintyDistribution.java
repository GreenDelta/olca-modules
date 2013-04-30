
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UncertaintyDistributionTypeValues.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="UncertaintyDistributionTypeValues">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="undefined"/>
 *     &lt;enumeration value="log-normal"/>
 *     &lt;enumeration value="normal"/>
 *     &lt;enumeration value="triangular"/>
 *     &lt;enumeration value="uniform"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "UncertaintyDistributionTypeValues")
@XmlEnum
public enum UncertaintyDistribution {


    /**
     * Probability distribution information not available.
     * 
     */
    @XmlEnumValue("undefined")
    UNDEFINED("undefined"),

    /**
     * Probability distribution of any random parameter whose logarithm is normally distributed.
     * 
     */
    @XmlEnumValue("log-normal")
    LOG_NORMAL("log-normal"),

    /**
     * Probability distribution of any random parameter whose value is normally distributed around the mean of zero.
     * 
     */
    @XmlEnumValue("normal")
    NORMAL("normal"),

    /**
     * Probability distribution of any random parameter between minimum value and maximum value with the highest probability at the average value of
     *                         minimum plus maximum value. Linear change of probability between minimum, maximum and average value.
     * 
     */
    @XmlEnumValue("triangular")
    TRIANGULAR("triangular"),

    /**
     * Continuous uniform probability distribution between minimum value and maximum value and "0" probability beyond these.
     * 
     */
    @XmlEnumValue("uniform")
    UNIFORM("uniform");
    private final String value;

    UncertaintyDistribution(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static UncertaintyDistribution fromValue(String v) {
        for (UncertaintyDistribution c: UncertaintyDistribution.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
