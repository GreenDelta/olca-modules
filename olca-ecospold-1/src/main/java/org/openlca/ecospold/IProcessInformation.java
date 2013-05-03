package org.openlca.ecospold;

import java.util.List;

import org.w3c.dom.Element;

public interface IProcessInformation {

	/**
	 * Gets the value of the referenceFunction property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link IReferenceFunction }
	 *     
	 */
	public abstract IReferenceFunction getReferenceFunction();

	/**
	 * Sets the value of the referenceFunction property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link IReferenceFunction }
	 *     
	 */
	public abstract void setReferenceFunction(IReferenceFunction value);

	/**
	 * Gets the value of the geography property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link IGeography }
	 *     
	 */
	public abstract IGeography getGeography();

	/**
	 * Sets the value of the geography property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link IGeography }
	 *     
	 */
	public abstract void setGeography(IGeography value);

	/**
	 * Gets the value of the technology property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link ITechnology }
	 *     
	 */
	public abstract ITechnology getTechnology();

	/**
	 * Sets the value of the technology property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link ITechnology }
	 *     
	 */
	public abstract void setTechnology(ITechnology value);

	/**
	 * Gets the value of the timePeriod property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link ITimePeriod }
	 *     
	 */
	public abstract ITimePeriod getTimePeriod();

	/**
	 * Sets the value of the timePeriod property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link ITimePeriod }
	 *     
	 */
	public abstract void setTimePeriod(ITimePeriod value);

	/**
	 * Gets the value of the dataSetInformation property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link IDataSetInformation }
	 *     
	 */
	public abstract IDataSetInformation getDataSetInformation();

	/**
	 * Sets the value of the dataSetInformation property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link IDataSetInformation }
	 *     
	 */
	public abstract void setDataSetInformation(IDataSetInformation value);

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

}