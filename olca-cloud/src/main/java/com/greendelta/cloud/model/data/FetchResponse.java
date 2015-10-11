package com.greendelta.cloud.model.data;

import java.util.List;

public class FetchResponse {

	private List<FetchData> data;
	private String latestCommitId;

	public void setData(List<FetchData> data) {
		this.data = data;
	}

	public List<FetchData> getData() {
		return data;
	}

	public void setLatestCommitId(String latestCommitId) {
		this.latestCommitId = latestCommitId;
	}

	public String getLatestCommitId() {
		return latestCommitId;
	}

}
