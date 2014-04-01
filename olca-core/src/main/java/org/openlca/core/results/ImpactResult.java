package org.openlca.core.results;

import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public class ImpactResult {

	private ImpactCategoryDescriptor impactCategory;
	private double value;

	void setImpactCategory(ImpactCategoryDescriptor impactCategory) {
		this.impactCategory = impactCategory;
	}

	void setValue(double value) {
		this.value = value;
	}

	public ImpactCategoryDescriptor getImpactCategory() {
		return impactCategory;
	}

	public double getValue() {
		return value;
	}

}
