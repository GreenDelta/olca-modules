package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class ParameterDescriptor extends RootDescriptor {

	public ParameterDescriptor() {
		this.type = ModelType.PARAMETER;
	}

	@Override
	public ParameterDescriptor copy() {
		var copy = new ParameterDescriptor();
		copyFields(this, copy);
		return copy;
	}

	public static Builder create() {
		return new Builder(new ParameterDescriptor());
	}

	public static class Builder extends DescriptorBuilder<ParameterDescriptor> {
		private Builder(ParameterDescriptor descriptor) {
			super(descriptor);
		}
	}
}
