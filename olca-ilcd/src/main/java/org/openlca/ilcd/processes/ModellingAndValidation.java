
package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.openlca.ilcd.commons.Other;


/**
 * <p>Java class for ModellingAndValidationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ModellingAndValidationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="LCIMethodAndAllocation" type="{http://lca.jrc.it/ILCD/Process}LCIMethodAndAllocationType" minOccurs="0"/>
 *         &lt;element name="dataSourcesTreatmentAndRepresentativeness" type="{http://lca.jrc.it/ILCD/Process}DataSourcesTreatmentAndRepresentativenessType" minOccurs="0"/>
 *         &lt;element name="completeness" type="{http://lca.jrc.it/ILCD/Process}CompletenessType" minOccurs="0"/>
 *         &lt;element name="validation" type="{http://lca.jrc.it/ILCD/Process}ValidationType" minOccurs="0"/>
 *         &lt;element name="complianceDeclarations" type="{http://lca.jrc.it/ILCD/Process}ComplianceDeclarationsType" minOccurs="0"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}other" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModellingAndValidationType", propOrder = {
    "lciMethod",
    "representativeness",
    "completeness",
    "validation",
    "complianceDeclarations",
    "other"
})
public class ModellingAndValidation
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "LCIMethodAndAllocation")
    protected LCIMethod lciMethod;
    @XmlElement(name = "dataSourcesTreatmentAndRepresentativeness")
    protected Representativeness representativeness;
    protected Completeness completeness;
    protected Validation validation;
    protected ComplianceDeclarationList complianceDeclarations;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected Other other;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes =  new HashMap<>();

    /**
     * Gets the value of the lciMethod property.
     * 
     * @return
     *     possible object is
     *     {@link LCIMethod }
     *     
     */
    public LCIMethod getLciMethod() {
        return lciMethod;
    }

    /**
     * Sets the value of the lciMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link LCIMethod }
     *     
     */
    public void setLciMethod(LCIMethod value) {
        this.lciMethod = value;
    }

    /**
     * Gets the value of the representativeness property.
     * 
     * @return
     *     possible object is
     *     {@link Representativeness }
     *     
     */
    public Representativeness getRepresentativeness() {
        return representativeness;
    }

    /**
     * Sets the value of the representativeness property.
     * 
     * @param value
     *     allowed object is
     *     {@link Representativeness }
     *     
     */
    public void setRepresentativeness(Representativeness value) {
        this.representativeness = value;
    }

    /**
     * Gets the value of the completeness property.
     * 
     * @return
     *     possible object is
     *     {@link Completeness }
     *     
     */
    public Completeness getCompleteness() {
        return completeness;
    }

    /**
     * Sets the value of the completeness property.
     * 
     * @param value
     *     allowed object is
     *     {@link Completeness }
     *     
     */
    public void setCompleteness(Completeness value) {
        this.completeness = value;
    }

    /**
     * Gets the value of the validation property.
     * 
     * @return
     *     possible object is
     *     {@link Validation }
     *     
     */
    public Validation getValidation() {
        return validation;
    }

    /**
     * Sets the value of the validation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Validation }
     *     
     */
    public void setValidation(Validation value) {
        this.validation = value;
    }

    /**
     * Gets the value of the complianceDeclarations property.
     * 
     * @return
     *     possible object is
     *     {@link ComplianceDeclarationList }
     *     
     */
    public ComplianceDeclarationList getComplianceDeclarations() {
        return complianceDeclarations;
    }

    /**
     * Sets the value of the complianceDeclarations property.
     * 
     * @param value
     *     allowed object is
     *     {@link ComplianceDeclarationList }
     *     
     */
    public void setComplianceDeclarations(ComplianceDeclarationList value) {
        this.complianceDeclarations = value;
    }

    /**
     * Gets the value of the other property.
     * 
     * @return
     *     possible object is
     *     {@link Other }
     *     
     */
    public Other getOther() {
        return other;
    }

    /**
     * Sets the value of the other property.
     * 
     * @param value
     *     allowed object is
     *     {@link Other }
     *     
     */
    public void setOther(Other value) {
        this.other = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
