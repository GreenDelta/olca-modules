package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.enums.ParameterType;

abstract class SPParameter {

	private String name;
	private String comment;
	private ParameterType type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public ParameterType getType() {
		return type;
	}

	public void setType(ParameterType type) {
		this.type = type;
	}
}
