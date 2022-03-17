package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class DQSystemDescriptor extends RootDescriptor {

	public DQSystemDescriptor() {
		this.type = ModelType.DQ_SYSTEM;
	}

	@Override
	public DQSystemDescriptor copy() {
		var copy = new DQSystemDescriptor();
		copyFields(this, copy);
		return copy;
	}

	public static Builder create() {
		return new Builder(new DQSystemDescriptor());
	}

	public static class Builder extends DescriptorBuilder<DQSystemDescriptor> {

		private Builder(DQSystemDescriptor descriptor) {
			super(descriptor);
		}
	}
}
