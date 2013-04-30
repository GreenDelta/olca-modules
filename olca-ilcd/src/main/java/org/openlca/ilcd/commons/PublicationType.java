
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PublicationTypeValues.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="PublicationTypeValues">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Undefined"/>
 *     &lt;enumeration value="Article in periodical"/>
 *     &lt;enumeration value="Chapter in anthology"/>
 *     &lt;enumeration value="Monograph"/>
 *     &lt;enumeration value="Direct measurement"/>
 *     &lt;enumeration value="Oral communication"/>
 *     &lt;enumeration value="Personal written communication"/>
 *     &lt;enumeration value="Questionnaire"/>
 *     &lt;enumeration value="Software or database"/>
 *     &lt;enumeration value="Other unpublished and grey literature"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PublicationTypeValues")
@XmlEnum
public enum PublicationType {


    /**
     * Mixed, unclear or not defined publication type.
     * 
     */
    @XmlEnumValue("Undefined")
    UNDEFINED("Undefined"),

    /**
     * Printed article in periodical e.g. a journal paper
     * 
     */
    @XmlEnumValue("Article in periodical")
    ARTICLE_IN_PERIODICAL("Article in periodical"),

    /**
     * Chapter with distinct author in monograph with own editor.
     * 
     */
    @XmlEnumValue("Chapter in anthology")
    CHAPTER_IN_ANTHOLOGY("Chapter in anthology"),

    /**
     * Non-serial publication complete in one volume or a finite number of volumes. Including patents.
     * 
     */
    @XmlEnumValue("Monograph")
    MONOGRAPH("Monograph"),

    /**
     * Measured directly from the process to be represented.
     * 
     */
    @XmlEnumValue("Direct measurement")
    DIRECT_MEASUREMENT("Direct measurement"),

    /**
     * Direct or remote/phone oral information.
     * 
     */
    @XmlEnumValue("Oral communication")
    ORAL_COMMUNICATION("Oral communication"),

    /**
     * By individual to individual via letter, fax, email etc.
     * 
     */
    @XmlEnumValue("Personal written communication")
    PERSONAL_WRITTEN_COMMUNICATION("Personal written communication"),

    /**
     * Data received by a questionnaire targeting to data input for LCA use. Including existing data previously measured on site by process owner or
     *                         operator and provided officially in written form for LCA purposes. Excludes data transferred from elsewhere published data.
     * 
     */
    @XmlEnumValue("Questionnaire")
    QUESTIONNAIRE("Questionnaire"),

    /**
     * Software tools or databases, based on mixed sources, e.g. LCA databases.
     * 
     */
    @XmlEnumValue("Software or database")
    SOFTWARE_OR_DATABASE("Software or database"),

    /**
     * Including internal reports for other purposes than LCA, confidential LCA studies, master theses, etc. Includes also
     *                         websites.
     * 
     */
    @XmlEnumValue("Other unpublished and grey literature")
    OTHER_UNPUBLISHED_AND_GREY_LITERATURE("Other unpublished and grey literature");
    private final String value;

    PublicationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PublicationType fromValue(String v) {
        for (PublicationType c: PublicationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
