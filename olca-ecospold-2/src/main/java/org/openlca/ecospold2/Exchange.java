package org.openlca.ecospold2;

public abstract class Exchange {

	private String id;
	private String unitId;
	private double amount;
	private String name;
	private String unitName;
	private String mathematicalRelation;
	private String comment;
	private Integer outputGroup;
	private Integer inputGroup;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getMathematicalRelation() {
		return mathematicalRelation;
	}

	public void setMathematicalRelation(String mathematicalRelation) {
		this.mathematicalRelation = mathematicalRelation;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Integer getInputGroup() {
		return inputGroup;
	}

	public Integer getOutputGroup() {
		return outputGroup;
	}

	public void setInputGroup(Integer inputGroup) {
		this.inputGroup = inputGroup;
	}

	public void setOutputGroup(Integer outputGroup) {
		this.outputGroup = outputGroup;
	}

}
