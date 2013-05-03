package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

public class DataSet {

	private Activity activity;
	private List<Classification> classifications = new ArrayList<>();

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public List<Classification> getClassifications() {
		return classifications;
	}

}
