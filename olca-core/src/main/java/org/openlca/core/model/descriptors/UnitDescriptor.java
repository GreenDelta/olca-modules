package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class UnitDescriptor extends Descriptor {

	public UnitDescriptor() {
		this.type = ModelType.UNIT;
	}

	@Override
	public UnitDescriptor copy() {
		var copy = new UnitDescriptor();
		copyFields(this, copy);
		return copy;
	}

	public static Builder create() {
		return new Builder(new UnitDescriptor());
	}

	public static class Builder extends DescriptorBuilder<UnitDescriptor> {
		private Builder(UnitDescriptor descriptor) {
			super(descriptor);
		}
	}
}
