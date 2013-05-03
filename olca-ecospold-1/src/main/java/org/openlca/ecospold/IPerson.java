package org.openlca.ecospold;


public interface IPerson {

	/**
	 * Gets the value of the number property.
	 * 
	 */
	public abstract int getNumber();

	/**
	 * Sets the value of the number property.
	 * 
	 */
	public abstract void setNumber(int value);

	/**
	 * Gets the value of the name property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getName();

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setName(String value);

	/**
	 * Gets the value of the address property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getAddress();

	/**
	 * Sets the value of the address property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setAddress(String value);

	/**
	 * Gets the value of the telephone property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getTelephone();

	/**
	 * Sets the value of the telephone property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setTelephone(String value);

	/**
	 * Gets the value of the telefax property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getTelefax();

	/**
	 * Sets the value of the telefax property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setTelefax(String value);

	/**
	 * Gets the value of the email property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getEmail();

	/**
	 * Sets the value of the email property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setEmail(String value);

	/**
	 * Gets the value of the companyCode property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getCompanyCode();

	/**
	 * Sets the value of the companyCode property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setCompanyCode(String value);

	/**
	 * Gets the value of the countryCode property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link ICountryCode }
	 *     
	 */
	public abstract ICountryCode getCountryCode();

	/**
	 * Sets the value of the countryCode property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link ICountryCode }
	 *     
	 */
	public abstract void setCountryCode(ICountryCode value);

}