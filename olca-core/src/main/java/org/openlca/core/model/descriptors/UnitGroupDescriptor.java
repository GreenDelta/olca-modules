package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class UnitGroupDescriptor extends RootDescriptor {

	public UnitGroupDescriptor() {
		this.type = ModelType.UNIT_GROUP;
	}

	@Override
	public UnitGroupDescriptor copy() {
		var copy = new UnitGroupDescriptor();
		copyFields(this, copy);
		return copy;
	}

	public static Builder create() {
		return new Builder(new UnitGroupDescriptor());
	}

	public static class Builder extends DescriptorBuilder<UnitGroupDescriptor> {
		private Builder(UnitGroupDescriptor descriptor) {
			super(descriptor);
		}
	}
}
