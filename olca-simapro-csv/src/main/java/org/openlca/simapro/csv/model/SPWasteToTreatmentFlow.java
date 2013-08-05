package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.types.DistributionType;

public class SPWasteToTreatmentFlow extends SPFlow {

	/**
	 * The name of the flow
	 */
	String name;

	/**
	 * The distribution of the flow
	 */
	private DistributionType distributionType = DistributionType.UNDEFINED;
	private String standardDeviation = "0";
	private String min = "0";
	private String max = "0";

	public SPWasteToTreatmentFlow(String amount, SPUnit unit, String comment) {
		super(amount, unit, comment);
		// name;unit;amount;Distribution;standardDeviation(0);Min(0);Max(0);
		// name;kg;1;Undefined;0;0;0;
	}

	@Override
	public void setName(String name) {
		this.name = name;

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

	@Override
	public String getName() {
		return name;
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
