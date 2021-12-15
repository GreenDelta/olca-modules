package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class SourceDescriptor extends CategorizedDescriptor {

	public SourceDescriptor() {
		this.type = ModelType.SOURCE;
	}

	@Override
	public SourceDescriptor copy() {
		var copy = new SourceDescriptor();
		copyFields(this, copy);
		return copy;
	}

}
