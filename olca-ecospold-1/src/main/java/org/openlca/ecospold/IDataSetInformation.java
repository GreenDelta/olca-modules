package org.openlca.ecospold;

import javax.xml.datatype.XMLGregorianCalendar;

public interface IDataSetInformation {

	/**
	 * Gets the value of the type property.
	 * 
	 */
	public abstract int getType();

	/**
	 * Sets the value of the type property.
	 * 
	 */
	public abstract void setType(int value);

	/**
	 * Gets the value of the impactAssessmentResult property.
	 * 
	 */
	public abstract boolean isImpactAssessmentResult();

	/**
	 * Sets the value of the impactAssessmentResult property.
	 * 
	 */
	public abstract void setImpactAssessmentResult(boolean value);

	/**
	 * Gets the value of the timestamp property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link XMLGregorianCalendar }
	 *     
	 */
	public abstract XMLGregorianCalendar getTimestamp();

	/**
	 * Sets the value of the timestamp property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link XMLGregorianCalendar }
	 *     
	 */
	public abstract void setTimestamp(XMLGregorianCalendar value);

	/**
	 * Gets the value of the version property.
	 * 
	 */
	public abstract float getVersion();

	/**
	 * Sets the value of the version property.
	 * 
	 */
	public abstract void setVersion(float value);

	/**
	 * Gets the value of the internalVersion property.
	 * 
	 */
	public abstract float getInternalVersion();

	/**
	 * Sets the value of the internalVersion property.
	 * 
	 */
	public abstract void setInternalVersion(float value);

	/**
	 * Gets the value of the energyValues property.
	 * 
	 */
	public abstract int getEnergyValues();

	/**
	 * Sets the value of the energyValues property.
	 * 
	 */
	public abstract void setEnergyValues(int value);

	/**
	 * Gets the value of the languageCode property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link ILanguageCode }
	 *     
	 */
	public abstract ILanguageCode getLanguageCode();

	/**
	 * Sets the value of the languageCode property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link ILanguageCode }
	 *     
	 */
	public abstract void setLanguageCode(ILanguageCode value);

	/**
	 * Gets the value of the localLanguageCode property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link ILanguageCode }
	 *     
	 */
	public abstract ILanguageCode getLocalLanguageCode();

	/**
	 * Sets the value of the localLanguageCode property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link ILanguageCode }
	 *     
	 */
	public abstract void setLocalLanguageCode(ILanguageCode value);

}