
package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}reference" minOccurs="0"/>
 *         &lt;element name="overallCompliance" type="{http://www.ilcd-network.org/ILCD/ServiceAPI}ComplianceValues" minOccurs="0"/>
 *         &lt;element name="nomenclatureCompliance" type="{http://www.ilcd-network.org/ILCD/ServiceAPI}ComplianceValues" minOccurs="0"/>
 *         &lt;element name="methodologicalCompliance" type="{http://www.ilcd-network.org/ILCD/ServiceAPI}ComplianceValues" minOccurs="0"/>
 *         &lt;element name="reviewCompliance" type="{http://www.ilcd-network.org/ILCD/ServiceAPI}ComplianceValues" minOccurs="0"/>
 *         &lt;element name="documentationCompliance" type="{http://www.ilcd-network.org/ILCD/ServiceAPI}ComplianceValues" minOccurs="0"/>
 *         &lt;element name="qualityCompliance" type="{http://www.ilcd-network.org/ILCD/ServiceAPI}ComplianceValues" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "reference",
    "overallCompliance",
    "nomenclatureCompliance",
    "methodologicalCompliance",
    "reviewCompliance",
    "documentationCompliance",
    "qualityCompliance"
})
@XmlRootElement(name = "complianceSystem")
public class ComplianceSystem
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
    protected DataSetReference reference;
    protected ComplianceValues overallCompliance;
    protected ComplianceValues nomenclatureCompliance;
    protected ComplianceValues methodologicalCompliance;
    protected ComplianceValues reviewCompliance;
    protected ComplianceValues documentationCompliance;
    protected ComplianceValues qualityCompliance;
    @XmlAttribute(name = "name")
    protected String name;

    /**
     * Gets the value of the reference property.
     * 
     * @return
     *     possible object is
     *     {@link DataSetReference }
     *     
     */
    public DataSetReference getReference() {
        return reference;
    }

    /**
     * Sets the value of the reference property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataSetReference }
     *     
     */
    public void setReference(DataSetReference value) {
        this.reference = value;
    }

    /**
     * Gets the value of the overallCompliance property.
     * 
     * @return
     *     possible object is
     *     {@link ComplianceValues }
     *     
     */
    public ComplianceValues getOverallCompliance() {
        return overallCompliance;
    }

    /**
     * Sets the value of the overallCompliance property.
     * 
     * @param value
     *     allowed object is
     *     {@link ComplianceValues }
     *     
     */
    public void setOverallCompliance(ComplianceValues value) {
        this.overallCompliance = value;
    }

    /**
     * Gets the value of the nomenclatureCompliance property.
     * 
     * @return
     *     possible object is
     *     {@link ComplianceValues }
     *     
     */
    public ComplianceValues getNomenclatureCompliance() {
        return nomenclatureCompliance;
    }

    /**
     * Sets the value of the nomenclatureCompliance property.
     * 
     * @param value
     *     allowed object is
     *     {@link ComplianceValues }
     *     
     */
    public void setNomenclatureCompliance(ComplianceValues value) {
        this.nomenclatureCompliance = value;
    }

    /**
     * Gets the value of the methodologicalCompliance property.
     * 
     * @return
     *     possible object is
     *     {@link ComplianceValues }
     *     
     */
    public ComplianceValues getMethodologicalCompliance() {
        return methodologicalCompliance;
    }

    /**
     * Sets the value of the methodologicalCompliance property.
     * 
     * @param value
     *     allowed object is
     *     {@link ComplianceValues }
     *     
     */
    public void setMethodologicalCompliance(ComplianceValues value) {
        this.methodologicalCompliance = value;
    }

    /**
     * Gets the value of the reviewCompliance property.
     * 
     * @return
     *     possible object is
     *     {@link ComplianceValues }
     *     
     */
    public ComplianceValues getReviewCompliance() {
        return reviewCompliance;
    }

    /**
     * Sets the value of the reviewCompliance property.
     * 
     * @param value
     *     allowed object is
     *     {@link ComplianceValues }
     *     
     */
    public void setReviewCompliance(ComplianceValues value) {
        this.reviewCompliance = value;
    }

    /**
     * Gets the value of the documentationCompliance property.
     * 
     * @return
     *     possible object is
     *     {@link ComplianceValues }
     *     
     */
    public ComplianceValues getDocumentationCompliance() {
        return documentationCompliance;
    }

    /**
     * Sets the value of the documentationCompliance property.
     * 
     * @param value
     *     allowed object is
     *     {@link ComplianceValues }
     *     
     */
    public void setDocumentationCompliance(ComplianceValues value) {
        this.documentationCompliance = value;
    }

    /**
     * Gets the value of the qualityCompliance property.
     * 
     * @return
     *     possible object is
     *     {@link ComplianceValues }
     *     
     */
    public ComplianceValues getQualityCompliance() {
        return qualityCompliance;
    }

    /**
     * Sets the value of the qualityCompliance property.
     * 
     * @param value
     *     allowed object is
     *     {@link ComplianceValues }
     *     
     */
    public void setQualityCompliance(ComplianceValues value) {
        this.qualityCompliance = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}
