
package org.openlca.ecospold.internal.impact;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ecospold.IGeography;


/**
 * Contains information about the geographic validity of the process. The region described with regional code and free text is the market area of the product / service at issue and not necessarily the place of production.
 * 
 * <p>Java class for TGeography complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TGeography">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="location" use="required" type="{http://www.EcoInvent.org/EcoSpold01Impact}TRegionalCode" />
 *       &lt;attribute name="text" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString32000" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TGeography")
class ImpactGeography
    implements Serializable, IGeography
{

    private final static long serialVersionUID = 1L;
    @XmlAttribute(name = "location", required = true)
    protected String location;
    @XmlAttribute(name = "text")
    protected String text;

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Override
	public String getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Override
	public void setLocation(String value) {
        this.location = value;
    }

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Override
	public String getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Override
	public void setText(String value) {
        this.text = value;
    }

}
