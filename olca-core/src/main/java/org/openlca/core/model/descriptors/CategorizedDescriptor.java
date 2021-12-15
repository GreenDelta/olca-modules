package org.openlca.core.model.descriptors;

public class CategorizedDescriptor extends Descriptor {

	public Long category;

	@Override
	public CategorizedDescriptor copy() {
		var copy = new CategorizedDescriptor();
		copyFields(this, copy);
		return copy;
	}

}
