package org.openlca.core.model.descriptors;

import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;

public class FlowDescriptor extends RootDescriptor {

	public Long location;
	public FlowType flowType;
	public long refFlowPropertyId;

	public FlowDescriptor() {
		this.type = ModelType.FLOW;
	}

	@Override
	public FlowDescriptor copy() {
		var copy = new FlowDescriptor();
		copyFields(this, copy);
		copy.location = location;
		copy.flowType = flowType;
		copy.refFlowPropertyId = refFlowPropertyId;
		return copy;
	}

	public static Builder create() {
		return new Builder(new FlowDescriptor());
	}

	public static class Builder extends DescriptorBuilder<FlowDescriptor> {

		private Builder(FlowDescriptor descriptor) {
			super(descriptor);
		}

		public Builder location(Long location) {
			descriptor.location = location;
			return this;
		}

		public Builder flowType(FlowType flowType) {
			descriptor.flowType = flowType;
			return this;
		}

		public Builder refFlowPropertyId(long refFlowPropertyId) {
			descriptor.refFlowPropertyId = refFlowPropertyId;
			return this;
		}
	}

}
