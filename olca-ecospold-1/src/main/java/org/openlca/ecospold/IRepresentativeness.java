package org.openlca.ecospold;

public interface IRepresentativeness {

	/**
	 * Gets the value of the percent property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link Float }
	 *     
	 */
	public abstract Float getPercent();

	/**
	 * Sets the value of the percent property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link Float }
	 *     
	 */
	public abstract void setPercent(Float value);

	/**
	 * Gets the value of the productionVolume property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getProductionVolume();

	/**
	 * Sets the value of the productionVolume property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setProductionVolume(String value);

	/**
	 * Gets the value of the samplingProcedure property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getSamplingProcedure();

	/**
	 * Sets the value of the samplingProcedure property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setSamplingProcedure(String value);

	/**
	 * Gets the value of the extrapolations property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getExtrapolations();

	/**
	 * Sets the value of the extrapolations property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setExtrapolations(String value);

	/**
	 * Gets the value of the uncertaintyAdjustments property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getUncertaintyAdjustments();

	/**
	 * Sets the value of the uncertaintyAdjustments property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setUncertaintyAdjustments(String value);

}