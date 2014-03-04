package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

public class SPElementaryFlow extends SPFlow {

	private IDistribution distribution;
	private String subCompartment;
	private ElementaryFlowType type;

	public IDistribution getDistribution() {
		return distribution;
	}

	public String getSubCompartment() {
		return subCompartment;
	}

	public ElementaryFlowType getType() {
		return type;
	}

	public void setDistribution(IDistribution distribution) {
		this.distribution = distribution;
	}

	public void setSubCompartment(String subCompartment) {
		this.subCompartment = subCompartment;
	}

	public void setType(ElementaryFlowType type) {
		this.type = type;
	}
}
