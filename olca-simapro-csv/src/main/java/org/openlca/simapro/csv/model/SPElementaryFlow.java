package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.SubCompartment;

public class SPElementaryFlow extends SPFlow {

	private IDistribution distribution;
	private SubCompartment subCompartment = SubCompartment.UNSPECIFIED;
	private ElementaryFlowType type;

	public IDistribution getDistribution() {
		return distribution;
	}

	public SubCompartment getSubCompartment() {
		return subCompartment;
	}

	public ElementaryFlowType getType() {
		return type;
	}

	public void setDistribution(IDistribution distribution) {
		this.distribution = distribution;
	}

	public void setSubCompartment(SubCompartment subCompartment) {
		this.subCompartment = subCompartment;
	}

	public void setType(ElementaryFlowType type) {
		this.type = type;
	}
}
