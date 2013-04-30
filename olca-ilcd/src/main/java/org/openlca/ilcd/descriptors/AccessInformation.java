
package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}copyright" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}licenseType" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}useRestrictions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "copyright",
    "licenseType",
    "useRestrictions"
})
@XmlRootElement(name = "accessInformation")
public class AccessInformation
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    protected Boolean copyright;
    protected String licenseType;
    protected LangString useRestrictions;

    /**
     * Gets the value of the copyright property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCopyright() {
        return copyright;
    }

    /**
     * Sets the value of the copyright property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCopyright(Boolean value) {
        this.copyright = value;
    }

    /**
     * Gets the value of the licenseType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLicenseType() {
        return licenseType;
    }

    /**
     * Sets the value of the licenseType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLicenseType(String value) {
        this.licenseType = value;
    }

    /**
     * Gets the value of the useRestrictions property.
     * 
     * @return
     *     possible object is
     *     {@link LangString }
     *     
     */
    public LangString getUseRestrictions() {
        return useRestrictions;
    }

    /**
     * Sets the value of the useRestrictions property.
     * 
     * @param value
     *     allowed object is
     *     {@link LangString }
     *     
     */
    public void setUseRestrictions(LangString value) {
        this.useRestrictions = value;
    }

}
