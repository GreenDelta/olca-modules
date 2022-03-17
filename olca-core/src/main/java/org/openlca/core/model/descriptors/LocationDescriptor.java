package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class LocationDescriptor extends RootDescriptor {

	public String code;

	public LocationDescriptor() {
		this.type = ModelType.LOCATION;
	}

	@Override
	public LocationDescriptor copy() {
		var copy = new LocationDescriptor();
		copyFields(this, copy);
		copy.code = code;
		return copy;
	}

	public static Builder create() {
		return new Builder(new LocationDescriptor());
	}

	public static class Builder extends DescriptorBuilder<LocationDescriptor> {
		private Builder(LocationDescriptor descriptor) {
			super(descriptor);
		}
	}

}
