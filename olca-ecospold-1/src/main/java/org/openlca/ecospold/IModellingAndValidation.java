package org.openlca.ecospold;

import java.util.List;

import org.w3c.dom.Element;

public interface IModellingAndValidation {

	/**
	 * Gets the value of the representativeness property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link IRepresentativeness }
	 *     
	 */
	public abstract IRepresentativeness getRepresentativeness();

	/**
	 * Sets the value of the representativeness property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link IRepresentativeness }
	 *     
	 */
	public abstract void setRepresentativeness(IRepresentativeness value);

	/**
	 * Gets the value of the source property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list,
	 * not a snapshot. Therefore any modification you make to the
	 * returned list will be present inside the JAXB object.
	 * This is why there is not a <CODE>set</CODE> method for the source property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * <pre>
	 *    getSource().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ISource }
	 * 
	 * 
	 */
	public abstract List<ISource> getSource();

	/**
	 * Gets the value of the validation property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link IValidation }
	 *     
	 */
	public abstract IValidation getValidation();

	/**
	 * Sets the value of the validation property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link IValidation }
	 *     
	 */
	public abstract void setValidation(IValidation value);

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