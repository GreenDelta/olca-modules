package org.openlca.core.results;

import org.openlca.core.model.descriptors.ImpactDescriptor;

public class ImpactValue {

	public ImpactDescriptor impact;
	public double value;

	public ImpactValue() {
	}
	
	public ImpactValue(ImpactDescriptor impact, double value) {
		this.impact = impact;
		this.value = value;
	}

	public static ImpactValue of(ImpactDescriptor impact, double value) {
		return new ImpactValue(impact, value);
	}

}
