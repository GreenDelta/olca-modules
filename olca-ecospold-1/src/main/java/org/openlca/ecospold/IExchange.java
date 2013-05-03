package org.openlca.ecospold;

public interface IExchange {

	/**
	 * Gets the value of the inputGroup property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public abstract Integer getInputGroup();

	/**
	 * Sets the value of the inputGroup property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public abstract void setInputGroup(Integer value);

	/**
	 * Gets the value of the outputGroup property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public abstract Integer getOutputGroup();

	/**
	 * Sets the value of the outputGroup property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public abstract void setOutputGroup(Integer value);

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
	 * Gets the value of the category property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public abstract String getCategory();

	/**
	 * Sets the value of the category property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public abstract void setCategory(String value);

	/**
	 * Gets the value of the subCategory property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public abstract String getSubCategory();

	/**
	 * Sets the value of the subCategory property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public abstract void setSubCategory(String value);

	/**
	 * Gets the value of the localCategory property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public abstract String getLocalCategory();

	/**
	 * Sets the value of the localCategory property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public abstract void setLocalCategory(String value);

	/**
	 * Gets the value of the localSubCategory property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public abstract String getLocalSubCategory();

	/**
	 * Sets the value of the localSubCategory property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public abstract void setLocalSubCategory(String value);

	/**
	 * Gets the value of the casNumber property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public abstract String getCASNumber();

	/**
	 * Sets the value of the casNumber property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public abstract void setCASNumber(String value);

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public abstract String getName();

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public abstract void setName(String value);

	/**
	 * Gets the value of the location property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public abstract String getLocation();

	/**
	 * Sets the value of the location property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public abstract void setLocation(String value);

	/**
	 * Gets the value of the unit property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public abstract String getUnit();

	/**
	 * Sets the value of the unit property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public abstract void setUnit(String value);

	/**
	 * Gets the value of the meanValue property.
	 * 
	 */
	public abstract double getMeanValue();

	/**
	 * Sets the value of the meanValue property.
	 * 
	 */
	public abstract void setMeanValue(double value);

	/**
	 * Gets the value of the uncertaintyType property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public abstract Integer getUncertaintyType();

	/**
	 * Sets the value of the uncertaintyType property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public abstract void setUncertaintyType(Integer value);

	/**
	 * Gets the value of the standardDeviation95 property.
	 * 
	 * @return possible object is {@link Double }
	 * 
	 */
	public abstract Double getStandardDeviation95();

	/**
	 * Sets the value of the standardDeviation95 property.
	 * 
	 * @param value
	 *            allowed object is {@link Double }
	 * 
	 */
	public abstract void setStandardDeviation95(Double value);

	/**
	 * Gets the value of the formula property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public abstract String getFormula();

	/**
	 * Sets the value of the formula property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public abstract void setFormula(String value);

	/**
	 * Gets the value of the referenceToSource property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public abstract Integer getReferenceToSource();

	/**
	 * Sets the value of the referenceToSource property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public abstract void setReferenceToSource(Integer value);

	/**
	 * Gets the value of the pageNumbers property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public abstract String getPageNumbers();

	/**
	 * Sets the value of the pageNumbers property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public abstract void setPageNumbers(String value);

	/**
	 * Gets the value of the generalComment property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public abstract String getGeneralComment();

	/**
	 * Sets the value of the generalComment property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public abstract void setGeneralComment(String value);

	/**
	 * Gets the value of the localName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public abstract String getLocalName();

	/**
	 * Sets the value of the localName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public abstract void setLocalName(String value);

	/**
	 * Gets the value of the infrastructureProcess property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public abstract Boolean isInfrastructureProcess();

	/**
	 * Sets the value of the infrastructureProcess property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public abstract void setInfrastructureProcess(Boolean value);

	/**
	 * Gets the value of the minValue property.
	 * 
	 * @return possible object is {@link Double }
	 * 
	 */
	public abstract Double getMinValue();

	/**
	 * Sets the value of the minValue property.
	 * 
	 * @param value
	 *            allowed object is {@link Double }
	 * 
	 */
	public abstract void setMinValue(Double value);

	/**
	 * Gets the value of the maxValue property.
	 * 
	 * @return possible object is {@link Double }
	 * 
	 */
	public abstract Double getMaxValue();

	/**
	 * Sets the value of the maxValue property.
	 * 
	 * @param value
	 *            allowed object is {@link Double }
	 * 
	 */
	public abstract void setMaxValue(Double value);

	/**
	 * Gets the value of the mostLikelyValue property.
	 * 
	 * @return possible object is {@link Double }
	 * 
	 */
	public abstract Double getMostLikelyValue();

	/**
	 * Sets the value of the mostLikelyValue property.
	 * 
	 * @param value
	 *            allowed object is {@link Double }
	 * 
	 */
	public abstract void setMostLikelyValue(Double value);

	/**
	 * Returns true if this exchange is an elementary flow. An exchange
	 * describes an elementary flow if it has an input group or output group
	 * with value 4. Additionally exchanges with no input group AND no output
	 * group (impact assessment factors) are recognised as elementary flows.
	 */
	public abstract boolean isElementaryFlow();

}