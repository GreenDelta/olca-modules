package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class EpdDescriptor extends RootDescriptor {

	public EpdDescriptor() {
		this.type = ModelType.EPD;
	}

	@Override
	public EpdDescriptor copy() {
		var copy = new EpdDescriptor();
		copyFields(this, copy);
		return copy;
	}

	public static Builder create() {
		return new Builder(new EpdDescriptor());
	}

	public static class Builder extends DescriptorBuilder<EpdDescriptor> {
		private Builder(EpdDescriptor descriptor) {
			super(descriptor);
		}
	}
}
