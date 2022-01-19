package org.openlca.core.results;

import java.util.Objects;

import org.openlca.core.model.descriptors.ImpactDescriptor;

public record ImpactValue(ImpactDescriptor impact, double value) {

	public ImpactValue(ImpactDescriptor impact, double value) {
		this.impact = Objects.requireNonNull(impact);
		this.value = value;
	}

	public static ImpactValue of(ImpactDescriptor impact, double value) {
		return new ImpactValue(impact, value);
	}

}
