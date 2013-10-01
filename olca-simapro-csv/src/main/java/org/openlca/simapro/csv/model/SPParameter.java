package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.types.ParameterType;

public class SPParameter {

	protected String name;
	protected String comment;
	protected ParameterType type;

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
