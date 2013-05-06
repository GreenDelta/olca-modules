package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

public class DataSet {

	private Activity activity;
	private List<Classification> classifications = new ArrayList<>();
	private Geography geography;
	private Technology technology;
	private TimePeriod timePeriod;

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public List<Classification> getClassifications() {
		return classifications;
	}

	public Geography getGeography() {
		return geography;
	}

	public void setGeography(Geography geography) {
		this.geography = geography;
	}

	public Technology getTechnology() {
		return technology;
	}

	public void setTechnology(Technology technology) {
		this.technology = technology;
	}

	public TimePeriod getTimePeriod() {
		return timePeriod;
	}

	public void setTimePeriod(TimePeriod timePeriod) {
		this.timePeriod = timePeriod;
	}

}
