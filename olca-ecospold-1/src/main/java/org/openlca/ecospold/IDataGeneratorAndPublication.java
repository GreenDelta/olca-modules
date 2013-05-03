package org.openlca.ecospold;

public interface IDataGeneratorAndPublication {

	/**
	 * Gets the value of the person property.
	 * 
	 */
	public abstract int getPerson();

	/**
	 * Sets the value of the person property.
	 * 
	 */
	public abstract void setPerson(int value);

	/**
	 * Gets the value of the dataPublishedIn property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link Integer }
	 *     
	 */
	public abstract int getDataPublishedIn();

	/**
	 * Sets the value of the dataPublishedIn property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link Integer }
	 *     
	 */
	public abstract void setDataPublishedIn(Integer value);

	/**
	 * Gets the value of the referenceToPublishedSource property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link Integer }
	 *     
	 */
	public abstract Integer getReferenceToPublishedSource();

	/**
	 * Sets the value of the referenceToPublishedSource property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link Integer }
	 *     
	 */
	public abstract void setReferenceToPublishedSource(Integer value);

	/**
	 * Gets the value of the copyright property.
	 * 
	 */
	public abstract boolean isCopyright();

	/**
	 * Sets the value of the copyright property.
	 * 
	 */
	public abstract void setCopyright(boolean value);

	/**
	 * Gets the value of the accessRestrictedTo property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link Integer }
	 *     
	 */
	public abstract Integer getAccessRestrictedTo();

	/**
	 * Sets the value of the accessRestrictedTo property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link Integer }
	 *     
	 */
	public abstract void setAccessRestrictedTo(Integer value);

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
	 *     {@link CountryCode }
	 *     
	 */
	public abstract ICountryCode getCountryCode();

	/**
	 * Sets the value of the countryCode property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link CountryCode }
	 *     
	 */
	public abstract void setCountryCode(ICountryCode value);

	/**
	 * Gets the value of the pageNumbers property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getPageNumbers();

	/**
	 * Sets the value of the pageNumbers property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setPageNumbers(String value);

}