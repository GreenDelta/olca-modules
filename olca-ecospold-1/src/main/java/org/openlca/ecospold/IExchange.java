package org.openlca.ecospold;

public interface IExchange {

	/**
	 * Gets the value of the inputGroup property.
	 *
	 * @return possible object is {@link Integer }
	 *
	 */
	Integer getInputGroup();

	/**
	 * Sets the value of the inputGroup property.
	 *
	 * @param value
	 *            allowed object is {@link Integer }
	 *
	 */
	void setInputGroup(Integer value);

	/**
	 * Gets the value of the outputGroup property.
	 *
	 * @return possible object is {@link Integer }
	 *
	 */
	Integer getOutputGroup();

	/**
	 * Sets the value of the outputGroup property.
	 *
	 * @param value
	 *            allowed object is {@link Integer }
	 *
	 */
	void setOutputGroup(Integer value);

	/**
	 * Gets the value of the number property.
	 *
	 */
	int getNumber();

	/**
	 * Sets the value of the number property.
	 *
	 */
	void setNumber(int value);

	/**
	 * Gets the value of the category property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	String getCategory();

	/**
	 * Sets the value of the category property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	void setCategory(String value);

	/**
	 * Gets the value of the subCategory property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	String getSubCategory();

	/**
	 * Sets the value of the subCategory property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	void setSubCategory(String value);

	/**
	 * Gets the value of the localCategory property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	String getLocalCategory();

	/**
	 * Sets the value of the localCategory property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	void setLocalCategory(String value);

	/**
	 * Gets the value of the localSubCategory property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	String getLocalSubCategory();

	/**
	 * Sets the value of the localSubCategory property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	void setLocalSubCategory(String value);

	/**
	 * Gets the value of the casNumber property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	String getCASNumber();

	/**
	 * Sets the value of the casNumber property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	void setCASNumber(String value);

	/**
	 * Gets the value of the name property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	String getName();

	/**
	 * Sets the value of the name property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	void setName(String value);

	/**
	 * Gets the value of the location property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	String getLocation();

	/**
	 * Sets the value of the location property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	void setLocation(String value);

	/**
	 * Gets the value of the unit property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	String getUnit();

	/**
	 * Sets the value of the unit property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	void setUnit(String value);

	/**
	 * Gets the value of the meanValue property.
	 *
	 */
	double getMeanValue();

	/**
	 * Sets the value of the meanValue property.
	 *
	 */
	void setMeanValue(double value);

	/**
	 * Gets the value of the uncertaintyType property.
	 *
	 * @return possible object is {@link Integer }
	 *
	 */
	Integer getUncertaintyType();

	/**
	 * Sets the value of the uncertaintyType property.
	 *
	 * @param value
	 *            allowed object is {@link Integer }
	 *
	 */
	void setUncertaintyType(Integer value);

	/**
	 * Gets the value of the standardDeviation95 property.
	 *
	 * @return possible object is {@link Double }
	 *
	 */
	Double getStandardDeviation95();

	/**
	 * Sets the value of the standardDeviation95 property.
	 *
	 * @param value
	 *            allowed object is {@link Double }
	 *
	 */
	void setStandardDeviation95(Double value);

	/**
	 * Gets the value of the formula property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	String getFormula();

	/**
	 * Sets the value of the formula property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	void setFormula(String value);

	/**
	 * Gets the value of the referenceToSource property.
	 *
	 * @return possible object is {@link Integer }
	 *
	 */
	Integer getReferenceToSource();

	/**
	 * Sets the value of the referenceToSource property.
	 *
	 * @param value
	 *            allowed object is {@link Integer }
	 *
	 */
	void setReferenceToSource(Integer value);

	/**
	 * Gets the value of the pageNumbers property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	String getPageNumbers();

	/**
	 * Sets the value of the pageNumbers property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	void setPageNumbers(String value);

	/**
	 * Gets the value of the generalComment property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	String getGeneralComment();

	/**
	 * Sets the value of the generalComment property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	void setGeneralComment(String value);

	/**
	 * Gets the value of the localName property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	String getLocalName();

	/**
	 * Sets the value of the localName property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	void setLocalName(String value);

	/**
	 * Gets the value of the infrastructureProcess property.
	 *
	 * @return possible object is {@link Boolean }
	 *
	 */
	Boolean isInfrastructureProcess();

	/**
	 * Sets the value of the infrastructureProcess property.
	 *
	 * @param value
	 *            allowed object is {@link Boolean }
	 *
	 */
	void setInfrastructureProcess(Boolean value);

	/**
	 * Gets the value of the minValue property.
	 *
	 * @return possible object is {@link Double }
	 *
	 */
	Double getMinValue();

	/**
	 * Sets the value of the minValue property.
	 *
	 * @param value
	 *            allowed object is {@link Double }
	 *
	 */
	void setMinValue(Double value);

	/**
	 * Gets the value of the maxValue property.
	 *
	 * @return possible object is {@link Double }
	 *
	 */
	Double getMaxValue();

	/**
	 * Sets the value of the maxValue property.
	 *
	 * @param value
	 *            allowed object is {@link Double }
	 *
	 */
	void setMaxValue(Double value);

	/**
	 * Gets the value of the mostLikelyValue property.
	 *
	 * @return possible object is {@link Double }
	 *
	 */
	Double getMostLikelyValue();

	/**
	 * Sets the value of the mostLikelyValue property.
	 *
	 * @param value
	 *            allowed object is {@link Double }
	 *
	 */
	void setMostLikelyValue(Double value);

	/**
	 * Returns true if this exchange is an elementary flow. An exchange
	 * describes an elementary flow if it has an input group or output group
	 * with value 4. Additionally exchanges with no input group AND no output
	 * group (impact assessment factors) are recognised as elementary flows.
	 */
	default boolean isElementaryFlow() {
		var inGroup = getInputGroup();
		var outGroup = getOutputGroup();
		if (inGroup == null && outGroup == null)
			return true;
		return (inGroup != null && inGroup == 4)
				|| (outGroup != null && outGroup == 4);
	}

}
