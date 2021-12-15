package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class ResultDescriptor extends CategorizedDescriptor {

	public ResultDescriptor() {
		this.type = ModelType.RESULT;
	}

	@Override
	public ResultDescriptor copy() {
		var copy = new ResultDescriptor();
		copyFields(this, copy);
		return copy;
	}

}
