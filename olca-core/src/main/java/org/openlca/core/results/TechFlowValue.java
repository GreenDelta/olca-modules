package org.openlca.core.results;

import java.util.Objects;

import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;

public record TechFlowValue(TechFlow techFlow, double value) {

	public static TechFlowValue of(TechFlow techFlow, double value) {
		return new TechFlowValue(techFlow, value);
	}

	public TechFlowValue(TechFlow techFlow, double value) {
		this.techFlow = Objects.requireNonNull(techFlow);
		this.value = value;
	}

	public RootDescriptor provider() {
		return techFlow.provider();
	}

	public FlowDescriptor flow() {
		return techFlow.flow();
	}

}
