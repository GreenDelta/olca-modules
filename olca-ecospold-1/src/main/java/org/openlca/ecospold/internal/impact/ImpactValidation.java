
package org.openlca.ecospold.internal.impact;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ecospold.IValidation;


/**
 * Contains information about who carried out the critical review and about the main results and conclusions of the review and the recommendations made.
 * 
 * <p>Java class for TValidation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TValidation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="proofReadingDetails" use="required" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString32000" />
 *       &lt;attribute name="proofReadingValidator" use="required" type="{http://www.EcoInvent.org/EcoSpold01Impact}TIndexNumber" />
 *       &lt;attribute name="otherDetails" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString32000" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TValidation")
class ImpactValidation
    implements Serializable, IValidation
{

    private final static long serialVersionUID = 1L;
    @XmlAttribute(name = "proofReadingDetails", required = true)
    protected String proofReadingDetails;
    @XmlAttribute(name = "proofReadingValidator", required = true)
    protected int proofReadingValidator;
    @XmlAttribute(name = "otherDetails")
    protected String otherDetails;

    /**
     * Gets the value of the proofReadingDetails property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Override
	public String getProofReadingDetails() {
        return proofReadingDetails;
    }

    /**
     * Sets the value of the proofReadingDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Override
	public void setProofReadingDetails(String value) {
        this.proofReadingDetails = value;
    }

    /**
     * Gets the value of the proofReadingValidator property.
     * 
     */
    @Override
	public int getProofReadingValidator() {
        return proofReadingValidator;
    }

    /**
     * Sets the value of the proofReadingValidator property.
     * 
     */
    @Override
	public void setProofReadingValidator(int value) {
        this.proofReadingValidator = value;
    }

    /**
     * Gets the value of the otherDetails property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Override
	public String getOtherDetails() {
        return otherDetails;
    }

    /**
     * Sets the value of the otherDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Override
	public void setOtherDetails(String value) {
        this.otherDetails = value;
    }

}
