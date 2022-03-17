package org.openlca.core.model.descriptors;

public class RootDescriptor extends Descriptor {

	public Long category;

	@Override
	public RootDescriptor copy() {
		var copy = new RootDescriptor();
		copyFields(this, copy);
		return copy;
	}

}
