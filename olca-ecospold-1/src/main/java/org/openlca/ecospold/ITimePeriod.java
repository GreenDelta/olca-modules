package org.openlca.ecospold;

import javax.xml.datatype.XMLGregorianCalendar;

public interface ITimePeriod {

	/**
	 * Gets the value of the startYear property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link XMLGregorianCalendar }
	 *     
	 */
	public abstract XMLGregorianCalendar getStartYear();

	/**
	 * Sets the value of the startYear property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link XMLGregorianCalendar }
	 *     
	 */
	public abstract void setStartYear(XMLGregorianCalendar value);

	/**
	 * Gets the value of the startYearMonth property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link XMLGregorianCalendar }
	 *     
	 */
	public abstract XMLGregorianCalendar getStartYearMonth();

	/**
	 * Sets the value of the startYearMonth property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link XMLGregorianCalendar }
	 *     
	 */
	public abstract void setStartYearMonth(XMLGregorianCalendar value);

	/**
	 * Gets the value of the startDate property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link XMLGregorianCalendar }
	 *     
	 */
	public abstract XMLGregorianCalendar getStartDate();

	/**
	 * Sets the value of the startDate property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link XMLGregorianCalendar }
	 *     
	 */
	public abstract void setStartDate(XMLGregorianCalendar value);

	/**
	 * Gets the value of the endYear property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link XMLGregorianCalendar }
	 *     
	 */
	public abstract XMLGregorianCalendar getEndYear();

	/**
	 * Sets the value of the endYear property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link XMLGregorianCalendar }
	 *     
	 */
	public abstract void setEndYear(XMLGregorianCalendar value);

	/**
	 * Gets the value of the endYearMonth property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link XMLGregorianCalendar }
	 *     
	 */
	public abstract XMLGregorianCalendar getEndYearMonth();

	/**
	 * Sets the value of the endYearMonth property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link XMLGregorianCalendar }
	 *     
	 */
	public abstract void setEndYearMonth(XMLGregorianCalendar value);

	/**
	 * Gets the value of the endDate property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link XMLGregorianCalendar }
	 *     
	 */
	public abstract XMLGregorianCalendar getEndDate();

	/**
	 * Sets the value of the endDate property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link XMLGregorianCalendar }
	 *     
	 */
	public abstract void setEndDate(XMLGregorianCalendar value);

	/**
	 * Gets the value of the dataValidForEntirePeriod property.
	 * 
	 */
	public abstract boolean isDataValidForEntirePeriod();

	/**
	 * Sets the value of the dataValidForEntirePeriod property.
	 * 
	 */
	public abstract void setDataValidForEntirePeriod(boolean value);

	/**
	 * Gets the value of the text property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getText();

	/**
	 * Sets the value of the text property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setText(String value);

}