package org.openlca.ecospold;

import java.util.List;

public interface IAllocation {

	/**
	 * Gets the value of the referenceToInputOutput property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list,
	 * not a snapshot. Therefore any modification you make to the
	 * returned list will be present inside the JAXB object.
	 * This is why there is not a <CODE>set</CODE> method for the referenceToInputOutput property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * <pre>
	 *    getReferenceToInputOutput().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Integer }
	 * 
	 * 
	 */
	public abstract List<Integer> getReferenceToInputOutput();

	/**
	 * Gets the value of the referenceToCoProduct property.
	 * 
	 */
	public abstract int getReferenceToCoProduct();

	/**
	 * Sets the value of the referenceToCoProduct property.
	 * 
	 */
	public abstract void setReferenceToCoProduct(int value);

	/**
	 * Gets the value of the allocationMethod property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link Integer }
	 *     
	 */
	public abstract int getAllocationMethod();

	/**
	 * Sets the value of the allocationMethod property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link Integer }
	 *     
	 */
	public abstract void setAllocationMethod(Integer value);

	/**
	 * Gets the value of the fraction property.
	 * 
	 */
	public abstract float getFraction();

	/**
	 * Sets the value of the fraction property.
	 * 
	 */
	public abstract void setFraction(float value);

	/**
	 * Gets the value of the explanations property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getExplanations();

	/**
	 * Sets the value of the explanations property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setExplanations(String value);

}