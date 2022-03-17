package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class SourceDescriptor extends RootDescriptor {

	public SourceDescriptor() {
		this.type = ModelType.SOURCE;
	}

	@Override
	public SourceDescriptor copy() {
		var copy = new SourceDescriptor();
		copyFields(this, copy);
		return copy;
	}

	public static Builder create() {
		return new Builder(new SourceDescriptor());
	}

	public static class Builder extends DescriptorBuilder<SourceDescriptor> {
		private Builder(SourceDescriptor descriptor) {
			super(descriptor);
		}
	}
}
