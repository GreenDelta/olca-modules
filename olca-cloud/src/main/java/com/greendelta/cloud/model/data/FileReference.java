package com.greendelta.cloud.model.data;

import java.io.Serializable;

import org.openlca.core.model.ModelType;

public class FileReference implements Serializable {

	private static final long serialVersionUID = -6108676257021661077L;
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
