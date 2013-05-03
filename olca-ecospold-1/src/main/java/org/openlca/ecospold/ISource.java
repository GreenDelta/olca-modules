package org.openlca.ecospold;

import java.math.BigInteger;

import javax.xml.datatype.XMLGregorianCalendar;

public interface ISource {

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
	 * Gets the value of the sourceType property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link Integer }
	 *     
	 */
	public abstract int getSourceType();

	/**
	 * Sets the value of the sourceType property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link Integer }
	 *     
	 */
	public abstract void setSourceType(Integer value);

	/**
	 * Gets the value of the firstAuthor property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getFirstAuthor();

	/**
	 * Sets the value of the firstAuthor property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setFirstAuthor(String value);

	/**
	 * Gets the value of the additionalAuthors property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getAdditionalAuthors();

	/**
	 * Sets the value of the additionalAuthors property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setAdditionalAuthors(String value);

	/**
	 * Gets the value of the year property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link XMLGregorianCalendar }
	 *     
	 */
	public abstract XMLGregorianCalendar getYear();

	/**
	 * Sets the value of the year property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link XMLGregorianCalendar }
	 *     
	 */
	public abstract void setYear(XMLGregorianCalendar value);

	/**
	 * Gets the value of the title property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getTitle();

	/**
	 * Sets the value of the title property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setTitle(String value);

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

	/**
	 * Gets the value of the nameOfEditors property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getNameOfEditors();

	/**
	 * Sets the value of the nameOfEditors property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setNameOfEditors(String value);

	/**
	 * Gets the value of the titleOfAnthology property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getTitleOfAnthology();

	/**
	 * Sets the value of the titleOfAnthology property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setTitleOfAnthology(String value);

	/**
	 * Gets the value of the placeOfPublications property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getPlaceOfPublications();

	/**
	 * Sets the value of the placeOfPublications property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setPlaceOfPublications(String value);

	/**
	 * Gets the value of the publisher property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getPublisher();

	/**
	 * Sets the value of the publisher property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setPublisher(String value);

	/**
	 * Gets the value of the journal property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getJournal();

	/**
	 * Sets the value of the journal property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setJournal(String value);

	/**
	 * Gets the value of the volumeNo property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link BigInteger }
	 *     
	 */
	public abstract BigInteger getVolumeNo();

	/**
	 * Sets the value of the volumeNo property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link BigInteger }
	 *     
	 */
	public abstract void setVolumeNo(BigInteger value);

	/**
	 * Gets the value of the issueNo property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getIssueNo();

	/**
	 * Sets the value of the issueNo property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setIssueNo(String value);

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