package com.greendelta.cloud.model.data;

public class CommitData {

	private DatasetIdentifier identifier;
	private String json;

	public DatasetIdentifier getIdentifier() {
		return identifier;
	}

	public void setIdentifier(DatasetIdentifier identifier) {
		this.identifier = identifier;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getJson() {
		return json;
	}

}
