package org.openlca.cloud.model.data;

public class FetchRequestData extends Dataset {

	private static final long serialVersionUID = 417426973222267018L;
	private boolean deleted;
	private boolean added;

	public FetchRequestData() {

	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public void setAdded(boolean added) {
		this.added = added;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public boolean isAdded() {
		return added;
	}

	@Override
	public String toString() {
		return super.toString() + ", added: " + added + ", deleted: " + deleted;
	}

}
