package org.openlca.ecospold;

import java.util.List;

public interface IReferenceFunction {

	/**
	 * Gets the value of the synonym property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list,
	 * not a snapshot. Therefore any modification you make to the
	 * returned list will be present inside the JAXB object.
	 * This is why there is not a <CODE>set</CODE> method for the synonym property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * <pre>
	 *    getSynonym().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link String }
	 * 
	 * 
	 */
	public abstract List<String> getSynonym();

	/**
	 * Gets the value of the datasetRelatesToProduct property.
	 * 
	 */
	public abstract boolean isDatasetRelatesToProduct();

	/**
	 * Sets the value of the datasetRelatesToProduct property.
	 * 
	 */
	public abstract void setDatasetRelatesToProduct(boolean value);

	/**
	 * Gets the value of the name property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getName();

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setName(String value);

	/**
	 * Gets the value of the localName property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getLocalName();

	/**
	 * Sets the value of the localName property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setLocalName(String value);

	/**
	 * Gets the value of the infrastructureProcess property.
	 * 
	 */
	public abstract boolean isInfrastructureProcess();

	/**
	 * Sets the value of the infrastructureProcess property.
	 * 
	 */
	public abstract void setInfrastructureProcess(boolean value);

	/**
	 * Gets the value of the amount property.
	 * 
	 */
	public abstract double getAmount();

	/**
	 * Sets the value of the amount property.
	 * 
	 */
	public abstract void setAmount(double value);

	/**
	 * Gets the value of the unit property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getUnit();

	/**
	 * Sets the value of the unit property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setUnit(String value);

	/**
	 * Gets the value of the category property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getCategory();

	/**
	 * Sets the value of the category property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setCategory(String value);

	/**
	 * Gets the value of the subCategory property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getSubCategory();

	/**
	 * Sets the value of the subCategory property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setSubCategory(String value);

	/**
	 * Gets the value of the localCategory property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getLocalCategory();

	/**
	 * Sets the value of the localCategory property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setLocalCategory(String value);

	/**
	 * Gets the value of the localSubCategory property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getLocalSubCategory();

	/**
	 * Sets the value of the localSubCategory property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setLocalSubCategory(String value);

	/**
	 * Gets the value of the includedProcesses property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getIncludedProcesses();

	/**
	 * Sets the value of the includedProcesses property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setIncludedProcesses(String value);

	/**
	 * Gets the value of the generalComment property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getGeneralComment();

	/**
	 * Sets the value of the generalComment property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setGeneralComment(String value);

	/**
	 * Gets the value of the infrastructureIncluded property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link Boolean }
	 *     
	 */
	public abstract boolean isInfrastructureIncluded();

	/**
	 * Sets the value of the infrastructureIncluded property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link Boolean }
	 *     
	 */
	public abstract void setInfrastructureIncluded(Boolean value);

	/**
	 * Gets the value of the casNumber property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getCASNumber();

	/**
	 * Sets the value of the casNumber property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setCASNumber(String value);

	/**
	 * Gets the value of the statisticalClassification property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link Long }
	 *     
	 */
	public abstract Long getStatisticalClassification();

	/**
	 * Sets the value of the statisticalClassification property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link Long }
	 *     
	 */
	public abstract void setStatisticalClassification(Long value);

	/**
	 * Gets the value of the formula property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getFormula();

	/**
	 * Sets the value of the formula property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setFormula(String value);

}