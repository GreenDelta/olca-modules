package org.openlca.ecospold;

import java.math.BigInteger;
import java.util.List;

import org.w3c.dom.Element;

public interface IEcoSpold {

	/**
	 * Gets the value of the dataset property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list,
	 * not a snapshot. Therefore any modification you make to the
	 * returned list will be present inside the JAXB object.
	 * This is why there is not a <CODE>set</CODE> method for the dataset property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * <pre>
	 *    getDataset().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link IDataSet }
	 * 
	 * 
	 */
	public abstract List<IDataSet> getDataset();

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
	 * Gets the value of the validationId property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link BigInteger }
	 *     
	 */
	public abstract BigInteger getValidationId();

	/**
	 * Sets the value of the validationId property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link BigInteger }
	 *     
	 */
	public abstract void setValidationId(BigInteger value);

	/**
	 * Gets the value of the validationStatus property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getValidationStatus();

	/**
	 * Sets the value of the validationStatus property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setValidationStatus(String value);

}