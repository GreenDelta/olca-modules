package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

/**
 * This class represents a SimaPro substance
 */
public class SPSubstance {

	private String name;
	private String referenceUnit;
	private String casNumber;
	private String comment;

	ElementaryFlowType flowType;

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCASNumber() {
		return casNumber;
	}

	public String getName() {
		return name;
	}

	public String getReferenceUnit() {
		return referenceUnit;
	}

	public SPSubstance(String name, String referenceUnit) {
		this.name = name;
		this.referenceUnit = referenceUnit;
	}

	public SPSubstance(String name, String referenceUnit, String casNumber) {
		this.name = name;
		this.referenceUnit = referenceUnit;
		this.casNumber = casNumber;
	}

	public void setCASNumber(String casNumber) {
		this.casNumber = casNumber;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setReferenceUnit(String referenceUnit) {
		this.referenceUnit = referenceUnit;
	}

	public ElementaryFlowType getFlowType() {
		return flowType;
	}

	public void setFlowType(ElementaryFlowType flowType) {
		this.flowType = flowType;
	}
}
