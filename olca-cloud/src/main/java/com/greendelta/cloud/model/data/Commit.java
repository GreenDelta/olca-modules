package com.greendelta.cloud.model.data;

import java.util.ArrayList;
import java.util.List;

public class Commit {

	private String message;
	private List<CommitData> data = new ArrayList<>();

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<CommitData> getData() {
		return data;
	}
}
