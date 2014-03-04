package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.enums.ProcessCategory;
import org.openlca.simapro.csv.model.enums.ProductFlowType;

public class SPProductFlow extends SPExchange {

	private IDistribution distribution;
	private ProductFlowType type;
	private ProcessCategory processCategory;
	private String referenceCategory;

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

	public ProcessCategory getProcessCategory() {
		return processCategory;
	}

	public void setProcessCategory(ProcessCategory processCategory) {
		this.processCategory = processCategory;
	}

	public String getReferenceCategory() {
		return referenceCategory;
	}

	public void setReferenceCategory(String referenceCategory) {
		this.referenceCategory = referenceCategory;
	}

	public boolean hasReferenceData() {
		if (referenceCategory == null)
			return false;
		if (processCategory == null)
			return false;
		return true;
	}

}
