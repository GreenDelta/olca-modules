package com.greendelta.cloud.model.data;

public class FetchData extends DatasetDescriptor {

	private static final long serialVersionUID = 417426973222267018L;
	private String json;

	public FetchData() {

	}

	public FetchData(DatasetDescriptor descriptor) {
		setRefId(descriptor.getRefId());
		setType(descriptor.getType());
		setVersion(descriptor.getVersion());
		setLastChange(descriptor.getLastChange());
		setName(descriptor.getName());
		setCategoryRefId(descriptor.getCategoryRefId());
		setCategoryType(descriptor.getCategoryType());
		setFullPath(descriptor.getFullPath());
	}

	public boolean isDeleted() {
		return json == null || json.isEmpty();
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

}
