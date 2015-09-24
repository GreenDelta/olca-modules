package com.greendelta.cloud.model.data;

import java.util.List;
import java.util.Map;

import org.openlca.core.model.ModelType;

public class FetchResponse {

	private Map<ModelType, List<String>> data;
	private String latestCommitId;

	public void setData(Map<ModelType, List<String>> data) {
		this.data = data;
	}

	public Map<ModelType, List<String>> getData() {
		return data;
	}

	public void setLatestCommitId(String latestCommitId) {
		this.latestCommitId = latestCommitId;
	}

	public String getLatestCommitId() {
		return latestCommitId;
	}

}
