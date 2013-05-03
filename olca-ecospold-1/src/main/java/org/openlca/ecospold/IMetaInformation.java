package org.openlca.ecospold;

import java.util.List;

import org.w3c.dom.Element;

public interface IMetaInformation {

	/**
	 * Gets the value of the processInformation property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link IProcessInformation }
	 *     
	 */
	public abstract IProcessInformation getProcessInformation();

	/**
	 * Sets the value of the processInformation property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link IProcessInformation }
	 *     
	 */
	public abstract void setProcessInformation(IProcessInformation value);

	/**
	 * Gets the value of the modellingAndValidation property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link IModellingAndValidation }
	 *     
	 */
	public abstract IModellingAndValidation getModellingAndValidation();

	/**
	 * Sets the value of the modellingAndValidation property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link IModellingAndValidation }
	 *     
	 */
	public abstract void setModellingAndValidation(IModellingAndValidation value);

	/**
	 * Gets the value of the administrativeInformation property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link IAdministrativeInformation }
	 *     
	 */
	public abstract IAdministrativeInformation getAdministrativeInformation();

	/**
	 * Sets the value of the administrativeInformation property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link IAdministrativeInformation }
	 *     
	 */
	public abstract void setAdministrativeInformation(
			IAdministrativeInformation value);

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