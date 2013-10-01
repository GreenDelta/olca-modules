package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.types.ProductFlowType;

/**
 * This class represents a product input in SimaPro
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public class SPProductFlow extends SPFlow {

	private IDistribution distribution;
	private ProductFlowType type;

	public SPProductFlow(ProductFlowType type, String name, String unit,
			String amount) {
		super(amount, unit);
		this.type = type;
		this.name = name;
	}

	public SPProductFlow(ProductFlowType type, String name, String unit,
			String amount, String comment, IDistribution distribution) {
		super(amount, unit, comment);
		this.type = type;
		this.name = name;
		this.distribution = distribution;
	}

	public IDistribution getDistribution() {
		return distribution;
	}

	public ProductFlowType getType() {
		return type;
	}

	public void setDistribution(IDistribution distribution) {
		this.distribution = distribution;
	}

	public void setType(ProductFlowType type) {
		this.type = type;
	}

}
