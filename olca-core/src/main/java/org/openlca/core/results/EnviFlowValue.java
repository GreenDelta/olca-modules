package org.openlca.core.results;

import java.util.Objects;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;

public record EnviFlowValue(EnviFlow enviFlow, double value) {

	public EnviFlowValue(EnviFlow enviFlow, double value) {
		this.enviFlow = Objects.requireNonNull(enviFlow);
		this.value = value;
	}

	public static EnviFlowValue of(EnviFlow indexFlow, double value) {
		return new EnviFlowValue(indexFlow, value);
	}

	public boolean isInput() {
		return enviFlow.isInput();
	}

	public FlowDescriptor flow() {
		return enviFlow.flow();
	}

	public LocationDescriptor location() {
		return enviFlow.location();
	}

	public boolean hasLocation() {
		return enviFlow.location() != null;
	}
}
