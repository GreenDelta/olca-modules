package org.openlca.simapro.csv.model;

/**
 * This class represents the basic information of each flow line in SimaPro
 */
public abstract class SPFlow {

	/**
	 * Creates a new flow
	 * 
	 * @param amount
	 *            The amount of the flow
	 * @param unit
	 *            The unit of the flow
	 */
	public SPFlow(String amount, SPUnit unit) {
		this.amount = amount;
		this.unit = unit;
	}

	/**
	 * Creates a new flow
	 * 
	 * @param amount
	 *            The amount of the flow
	 * @param unit
	 *            The unit of the flow
	 */
	public SPFlow(String amount, SPUnit unit, String comment) {
		this.amount = amount;
		this.unit = unit;
		this.comment = comment;
	}

	/**
	 * The amount of the flow
	 */
	private String amount;

	/**
	 * A comment to the flow
	 */
	private String comment;

	/**
	 * The unit of the flow
	 */
	private SPUnit unit;

	/**
	 * Getter of the amount
	 * 
	 * @return The amount of the flow
	 */
	public String getAmount() {
		return amount;
	}

	/**
	 * Getter of the comment
	 * 
	 * @return The comment of the flow
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Getter of the unit
	 * 
	 * @see IUnit
	 * @return The unit of the flow
	 */
	public SPUnit getUnit() {
		return unit;
	}

	/**
	 * Setter of the unit
	 * 
	 * @param unit
	 *            The new unit
	 */
	public void setUnit(SPUnit unit) {
		this.unit = unit;
	}

	/**
	 * Setter of the amount
	 * 
	 * @param amount
	 *            The new amount
	 */
	public void setAmount(String amount) {
		this.amount = amount;
	}

	/**
	 * Setter of the comment
	 * 
	 * @param comment
	 *            The new comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	public abstract String getName();

	public abstract void setName(String name);

}
