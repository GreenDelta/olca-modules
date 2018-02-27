package org.openlca.cloud.model.data;

import java.util.ArrayList;

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

	public Dataset asDataset() {
		Dataset ds = new Dataset();
		ds.type = type;
		ds.refId = refId;
		ds.name = name;
		ds.categoryRefId = categoryRefId;
		ds.categoryType = categoryType;
		ds.version = version;
		ds.lastChange = lastChange;
		if (categories == null)
			return ds;
		ds.categories = new ArrayList<>(categories);
		return ds;
	}

	@Override
	public String toString() {
		return super.toString() + ", added: " + added + ", deleted: " + deleted;
	}

}
