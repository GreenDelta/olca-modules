package org.openlca.simapro.csv.model;

/**
 * This class represents a SimaPro substance
 * 
 * @author Sebastian Greve
 * 
 */
public class SPSubstance {

	/**
	 * The name of the substance
	 */
	private String name;

	/**
	 * The reference unit of the substance
	 */
	private SPUnit referenceUnit;

	/**
	 * The CAS number of the substance
	 */
	private String casNumber;

	/**
	 * The comment of the substance
	 */
	private String comment;

	/**
	 * Getter of the comment
	 * 
	 * @return The comment of the substance
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Setter of the comment
	 * 
	 * @param comment
	 *            The new comment of the substance
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Getter of the CAS number
	 * 
	 * @return The CAS number of the substance
	 */
	public String getCASNumber() {
		return casNumber;
	}

	/**
	 * Getter of the name
	 * 
	 * @return The name of the substance
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter of the reference unit
	 * 
	 * @return The reference unit of the substance
	 */
	public SPUnit getReferenceUnit() {
		return referenceUnit;
	}

	/**
	 * Creates a new substance
	 * 
	 * @param name
	 *            The name of the substance
	 * @param referenceUnit
	 *            The reference unit of the substance
	 */
	public SPSubstance(String name, SPUnit referenceUnit) {
		this.name = name;
		this.referenceUnit = referenceUnit;
	}

	/**
	 * Creates a new substance
	 * 
	 * @param name
	 *            The name of the substance
	 * @param referenceUnit
	 *            The reference unit of the substance
	 * @param casNumber
	 *            The CAS number of the substance
	 */
	public SPSubstance(String name, SPUnit referenceUnit, String casNumber) {
		this.name = name;
		this.referenceUnit = referenceUnit;
		this.casNumber = casNumber;
	}

	/**
	 * Setter of the CAS number
	 * 
	 * @param casNumber
	 *            The new CAS number
	 */
	public void setCASNumber(String casNumber) {
		this.casNumber = casNumber;
	}

	/**
	 * Setter of the name
	 * 
	 * @param name
	 *            The new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Setter of the reference unit
	 * 
	 * @param referenceUnit
	 *            The new reference unit
	 */
	public void setReferenceUnit(SPUnit referenceUnit) {
		this.referenceUnit = referenceUnit;
	}

}
