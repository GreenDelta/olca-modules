package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

public class DataSet {

	private Activity activity;
	private List<Classification> classifications = new ArrayList<>();
	private List<ElementaryExchange> elementaryExchanges = new ArrayList<>();
	private List<IntermediateExchange> intermediateExchanges = new ArrayList<>();

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public List<Classification> getClassifications() {
		return classifications;
	}

	public List<ElementaryExchange> getElementaryExchanges() {
		return elementaryExchanges;
	}

	public List<IntermediateExchange> getIntermediateExchanges() {
		return intermediateExchanges;
	}

}
