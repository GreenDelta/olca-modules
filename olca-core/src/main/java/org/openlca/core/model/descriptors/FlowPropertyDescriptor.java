package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class FlowPropertyDescriptor extends RootDescriptor {

	public FlowPropertyDescriptor() {
		this.type = ModelType.FLOW_PROPERTY;
	}

	@Override
	public FlowPropertyDescriptor copy() {
		var copy = new FlowPropertyDescriptor();
		copyFields(this, copy);
		return copy;
	}

	public static Builder create() {
		return new Builder(new FlowPropertyDescriptor());
	}

	public static class Builder extends DescriptorBuilder<FlowPropertyDescriptor> {
		private Builder(FlowPropertyDescriptor descriptor) {
			super(descriptor);
		}
	}
}
