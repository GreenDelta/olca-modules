package com.greendelta.cloud.model.data;

public class FetchRequestData extends DatasetIdentifier {

	private static final long serialVersionUID = 417426973222267018L;
	private boolean deleted;

	public FetchRequestData() {

	}

	public FetchRequestData(DatasetIdentifier identifier) {
		setRefId(identifier.getRefId());
		setType(identifier.getType());
		setVersion(identifier.getVersion());
		setLastChange(identifier.getLastChange());
		setName(identifier.getName());
		setCategoryRefId(identifier.getCategoryRefId());
		setCategoryType(identifier.getCategoryType());
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isDeleted() {
		return deleted;
	}

}
