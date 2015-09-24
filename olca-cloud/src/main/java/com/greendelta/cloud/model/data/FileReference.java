package com.greendelta.cloud.model.data;

import org.openlca.core.model.ModelType;

public class FileReference {

	private String refId;
	private ModelType type;

	public String getRefId() {
		return refId;
	}

	public void setRefId(String refId) {
		this.refId = refId;
	}

	public ModelType getType() {
		return type;
	}

	public void setType(ModelType type) {
		this.type = type;
	}

}
