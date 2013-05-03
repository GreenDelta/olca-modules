
package org.openlca.ecospold.internal.impact;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ecospold.ICountryCode;
import org.openlca.ecospold.IPerson;


/**
 * Used for the identification of members of the organisation / institute co-operating within a quality network (e.g., ecoinvent) referred to in the areas Validation, dataEntryBy and dataGeneratorAndPublication.
 * 
 * <p>Java class for TPerson complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TPerson">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="number" use="required" type="{http://www.EcoInvent.org/EcoSpold01Impact}TIndexNumber" />
 *       &lt;attribute name="name" use="required" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString40" />
 *       &lt;attribute name="address" use="required" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString255" />
 *       &lt;attribute name="telephone" use="required" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString40" />
 *       &lt;attribute name="telefax" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString40" />
 *       &lt;attribute name="email" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString80" />
 *       &lt;attribute name="companyCode" use="required" type="{http://www.EcoInvent.org/EcoSpold01Impact}TCompanyCode" />
 *       &lt;attribute name="countryCode" use="required" type="{http://www.EcoInvent.org/EcoSpold01Impact}TISOCountryCode" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TPerson")
class ImpactPerson
    implements Serializable, IPerson
{

    private final static long serialVersionUID = 1L;
    @XmlAttribute(name = "number", required = true)
    protected int number;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "address", required = true)
    protected String address;
    @XmlAttribute(name = "telephone", required = true)
    protected String telephone;
    @XmlAttribute(name = "telefax")
    protected String telefax;
    @XmlAttribute(name = "email")
    protected String email;
    @XmlAttribute(name = "companyCode", required = true)
    protected String companyCode;
    @XmlAttribute(name = "countryCode", required = true)
    protected ImpactCountryCode countryCode;

    /**
     * Gets the value of the number property.
     * 
     */
    @Override
	public int getNumber() {
        return number;
    }

    /**
     * Sets the value of the number property.
     * 
     */
    @Override
	public void setNumber(int value) {
        this.number = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Override
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
    @Override
	public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the address property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Override
	public String getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Override
	public void setAddress(String value) {
        this.address = value;
    }

    /**
     * Gets the value of the telephone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Override
	public String getTelephone() {
        return telephone;
    }

    /**
     * Sets the value of the telephone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Override
	public void setTelephone(String value) {
        this.telephone = value;
    }

    /**
     * Gets the value of the telefax property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Override
	public String getTelefax() {
        return telefax;
    }

    /**
     * Sets the value of the telefax property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Override
	public void setTelefax(String value) {
        this.telefax = value;
    }

    /**
     * Gets the value of the email property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Override
	public String getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Override
	public void setEmail(String value) {
        this.email = value;
    }

    /**
     * Gets the value of the companyCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Override
	public String getCompanyCode() {
        return companyCode;
    }

    /**
     * Sets the value of the companyCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Override
	public void setCompanyCode(String value) {
        this.companyCode = value;
    }

    /**
     * Gets the value of the countryCode property.
     * 
     * @return
     *     possible object is
     *     {@link ImpactCountryCode }
     *     
     */
    @Override
	public ICountryCode getCountryCode() {
        return countryCode;
    }

    /**
     * Sets the value of the countryCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link ImpactCountryCode }
     *     
     */
    @Override
	public void setCountryCode(ICountryCode value) {
    	if (value instanceof ImpactCountryCode) {
            this.countryCode = (ImpactCountryCode)value;
    	} else {
            this.countryCode = ImpactCountryCode.fromValue(value.value()); 		
    	}
    }

}
