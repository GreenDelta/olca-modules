package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.types.ElementaryFlowType;
import org.openlca.simapro.csv.model.types.SubCompartment;

/**
 * This class represents an elementary flow in SimaPro
 */
public class SPElementaryFlow extends SPFlow {

	private IDistribution distribution;
	private SubCompartment subCompartment = SubCompartment.UNSPECIFIED;
	private ElementaryFlowType type;

	public SPElementaryFlow() {

	}

	public SPElementaryFlow(ElementaryFlowType type, String name, String unit,
			String amount) {
		super(amount, unit);
		this.type = type;
		this.name = name;
	}

	public SPElementaryFlow(ElementaryFlowType type,
			SubCompartment subCompartment, String name, String unit,
			String amount) {
		super(amount, unit);
		this.type = type;
		this.subCompartment = subCompartment;
		this.name = name;
	}

	public SPElementaryFlow(ElementaryFlowType type,
			SubCompartment subCompartment, String name, String unit,
			String amount, String comment, IDistribution distribution) {
		super(amount, unit, comment);
		this.type = type;
		this.subCompartment = subCompartment;
		this.distribution = distribution;
		this.name = name;
	}

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
