package org.openlca.ecospold;

import java.math.BigInteger;

public interface IDataEntryBy {

	/**
	 * Gets the value of the person property.
	 * 
	 */
	public abstract int getPerson();

	/**
	 * Sets the value of the person property.
	 * 
	 */
	public abstract void setPerson(int value);

	/**
	 * Gets the value of the qualityNetwork property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link BigInteger }
	 *     
	 */
	public abstract BigInteger getQualityNetwork();

	/**
	 * Sets the value of the qualityNetwork property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link BigInteger }
	 *     
	 */
	public abstract void setQualityNetwork(BigInteger value);

}