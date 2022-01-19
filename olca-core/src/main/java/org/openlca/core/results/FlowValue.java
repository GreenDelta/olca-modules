package org.openlca.core.results;

import java.util.Objects;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;

public record FlowValue(EnviFlow indexFlow, double value) {

	public FlowValue(EnviFlow indexFlow, double value) {
		this.indexFlow = Objects.requireNonNull(indexFlow);
		this.value = value;
	}

	public static FlowValue of(EnviFlow indexFlow, double value) {
		return new FlowValue(indexFlow, value);
	}

	public boolean isInput() {
		return indexFlow.isInput();
	}

	public FlowDescriptor flow() {
		return indexFlow.flow();
	}

	public LocationDescriptor location() {
		return indexFlow.location();
	}

	public boolean hasLocation() {
		return indexFlow.location() != null;
	}
}
