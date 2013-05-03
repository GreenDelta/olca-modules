package org.openlca.ecospold;

import java.util.List;

import org.w3c.dom.Element;

public interface IAdministrativeInformation {

	/**
	 * Gets the value of the dataEntryBy property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link IDataEntryBy }
	 *     
	 */
	public abstract IDataEntryBy getDataEntryBy();

	/**
	 * Sets the value of the dataEntryBy property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link IDataEntryBy }
	 *     
	 */
	public abstract void setDataEntryBy(IDataEntryBy value);

	/**
	 * Gets the value of the dataGeneratorAndPublication property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link IDataGeneratorAndPublication }
	 *     
	 */
	public abstract IDataGeneratorAndPublication getDataGeneratorAndPublication();

	/**
	 * Sets the value of the dataGeneratorAndPublication property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link IDataGeneratorAndPublication }
	 *     
	 */
	public abstract void setDataGeneratorAndPublication(
			IDataGeneratorAndPublication value);

	/**
	 * Gets the value of the person property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list,
	 * not a snapshot. Therefore any modification you make to the
	 * returned list will be present inside the JAXB object.
	 * This is why there is not a <CODE>set</CODE> method for the person property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * <pre>
	 *    getPerson().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link IPerson }
	 * 
	 * 
	 */
	public abstract List<IPerson> getPerson();

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