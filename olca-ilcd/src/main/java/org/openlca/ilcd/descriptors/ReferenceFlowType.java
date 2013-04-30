
package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ReferenceFlowType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReferenceFlowType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.ilcd-network.org/ILCD/ServiceAPI}StringMultiLang" minOccurs="0"/>
 *         &lt;element name="flowProperty" type="{http://www.ilcd-network.org/ILCD/ServiceAPI}StringMultiLang" minOccurs="0"/>
 *         &lt;element name="unit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="meanValue" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI}reference" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{http://www.w3.org/1999/xlink}href"/>
 *       &lt;attribute name="internalId" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReferenceFlowType", propOrder = {
    "name",
    "flowProperty",
    "unit",
    "meanValue",
    "reference"
})
public class ReferenceFlowType
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    protected LangString name;
    protected LangString flowProperty;
    protected String unit;
    protected Double meanValue;
    @XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
    protected DataSetReference reference;
    @XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
    @XmlSchemaType(name = "anyURI")
    protected String href;
    @XmlAttribute(name = "internalId")
    protected Integer internalId;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link LangString }
     *     
     */
    public LangString getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link LangString }
     *     
     */
    public void setName(LangString value) {
        this.name = value;
    }

    /**
     * Gets the value of the flowProperty property.
     * 
     * @return
     *     possible object is
     *     {@link LangString }
     *     
     */
    public LangString getFlowProperty() {
        return flowProperty;
    }

    /**
     * Sets the value of the flowProperty property.
     * 
     * @param value
     *     allowed object is
     *     {@link LangString }
     *     
     */
    public void setFlowProperty(LangString value) {
        this.flowProperty = value;
    }

    /**
     * Gets the value of the unit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the value of the unit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnit(String value) {
        this.unit = value;
    }

    /**
     * Gets the value of the meanValue property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMeanValue() {
        return meanValue;
    }

    /**
     * Sets the value of the meanValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMeanValue(Double value) {
        this.meanValue = value;
    }

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
     * Gets the value of the href property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHref(String value) {
        this.href = value;
    }

    /**
     * Gets the value of the internalId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getInternalId() {
        return internalId;
    }

    /**
     * Sets the value of the internalId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setInternalId(Integer value) {
        this.internalId = value;
    }

}
