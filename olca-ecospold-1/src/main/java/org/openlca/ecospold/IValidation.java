package org.openlca.ecospold;

public interface IValidation {

	/**
	 * Gets the value of the proofReadingDetails property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getProofReadingDetails();

	/**
	 * Sets the value of the proofReadingDetails property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setProofReadingDetails(String value);

	/**
	 * Gets the value of the proofReadingValidator property.
	 * 
	 */
	public abstract int getProofReadingValidator();

	/**
	 * Sets the value of the proofReadingValidator property.
	 * 
	 */
	public abstract void setProofReadingValidator(int value);

	/**
	 * Gets the value of the otherDetails property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getOtherDetails();

	/**
	 * Sets the value of the otherDetails property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setOtherDetails(String value);

}