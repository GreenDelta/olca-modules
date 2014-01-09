package org.openlca.simapro.csv.model;

/**
 * This class represents the basic information of each flow line in SimaPro
 */
public abstract class SPFlow {

	protected String name;
	private String amount;
	private String comment;
	private String unit;

	public SPFlow() {

	}

	public SPFlow(String amount, String unit) {
		this.amount = amount;
		this.unit = unit;
	}

	public SPFlow(String amount, String unit, String comment) {
		this.amount = amount;
		this.unit = unit;
		this.comment = comment;
	}

	public String getAmount() {
		return amount;
	}

	public String getComment() {
		return comment;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
