package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class DQSystemDescriptor extends CategorizedDescriptor {

	public DQSystemDescriptor() {
		this.type = ModelType.DQ_SYSTEM;
	}

	@Override
	public DQSystemDescriptor copy() {
		var copy = new DQSystemDescriptor();
		copyFields(this, copy);
		return copy;
	}

}
