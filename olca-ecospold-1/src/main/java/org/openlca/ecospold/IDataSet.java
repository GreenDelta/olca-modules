package org.openlca.ecospold;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Element;

public interface IDataSet {

	/**
	 * Gets the value of the metaInformation property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link IMetaInformation }
	 *     
	 */
	public abstract IMetaInformation getMetaInformation();

	/**
	 * Sets the value of the metaInformation property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link IMetaInformation }
	 *     
	 */
	public abstract void setMetaInformation(IMetaInformation value);

	/**
	 * Gets the value of the flowData property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list,
	 * not a snapshot. Therefore any modification you make to the
	 * returned list will be present inside the JAXB object.
	 * This is why there is not a <CODE>set</CODE> method for the flowData property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * <pre>
	 *    getFlowData().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link IFlowData }
	 * 
	 * 
	 */
	public abstract List<IFlowData> getFlowData();

	/**
	 * Gets the value of the any property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list,
	 * not a snapshot. Therefore any modification you make to the
	 * returned list will be present inside the JAXB object.
	 * This is why there is not a <CODE>set</CODE> method for the any property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * <pre>
	 *    getAny().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Object }
	 * {@link Element }
	 * 
	 * 
	 */
	public abstract List<Object> getAny();

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
	 * Gets the value of the internalSchemaVersion property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getInternalSchemaVersion();

	/**
	 * Sets the value of the internalSchemaVersion property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setInternalSchemaVersion(String value);

	/**
	 * Gets the value of the generator property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getGenerator();

	/**
	 * Sets the value of the generator property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setGenerator(String value);

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
	 * Gets the value of the validCompanyCodes property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getValidCompanyCodes();

	/**
	 * Sets the value of the validCompanyCodes property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setValidCompanyCodes(String value);

	/**
	 * Gets the value of the validRegionalCodes property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getValidRegionalCodes();

	/**
	 * Sets the value of the validRegionalCodes property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setValidRegionalCodes(String value);

	/**
	 * Gets the value of the validCategories property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getValidCategories();

	/**
	 * Sets the value of the validCategories property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setValidCategories(String value);

	/**
	 * Gets the value of the validUnits property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getValidUnits();

	/**
	 * Sets the value of the validUnits property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setValidUnits(String value);

}