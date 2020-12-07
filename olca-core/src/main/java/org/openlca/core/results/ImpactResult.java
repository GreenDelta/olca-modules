package org.openlca.core.results;

import org.openlca.core.model.descriptors.ImpactDescriptor;

public class ImpactResult {

	public ImpactDescriptor impact;
	public double value;

	public ImpactResult() {
	}
	
	public ImpactResult(ImpactDescriptor impact, double value) {
		this.impact = impact;
		this.value = value;
	}

	public static ImpactResult of(ImpactDescriptor impact, double value) {
		return new ImpactResult(impact, value);
	}

}
