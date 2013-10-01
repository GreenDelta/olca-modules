package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.types.DistributionType;

public class SPWasteToTreatmentFlow extends SPFlow {

	private DistributionType distributionType = DistributionType.UNDEFINED;
	private String standardDeviation = "0";
	private String min = "0";
	private String max = "0";

	public SPWasteToTreatmentFlow(String amount, String unit, String comment) {
		super(amount, unit, comment);
	}

	public void setDistributionType(DistributionType distributionType) {
		this.distributionType = distributionType;
	}

	public void setStandardDeviation(String standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public DistributionType getDistributionType() {
		return distributionType;
	}

	public String getStandardDeviation() {
		return standardDeviation;
	}

	public String getMin() {
		return min;
	}

	public String getMax() {
		return max;
	}

}
