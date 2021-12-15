package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class ParameterDescriptor extends CategorizedDescriptor {

	public ParameterDescriptor() {
		this.type = ModelType.PARAMETER;
	}

	@Override
	public ParameterDescriptor copy() {
		var copy = new ParameterDescriptor();
		copyFields(this, copy);
		return copy;
	}

}
