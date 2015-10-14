package com.greendelta.cloud.model.data;

public class CommitData {

	private DatasetDescriptor descriptor;
	private String json;

	public DatasetDescriptor getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(DatasetDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getJson() {
		return json;
	}

}
