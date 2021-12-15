package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class FlowPropertyDescriptor extends CategorizedDescriptor {

	public FlowPropertyDescriptor() {
		this.type = ModelType.FLOW_PROPERTY;
	}

	@Override
	public FlowPropertyDescriptor copy() {
		var copy = new FlowPropertyDescriptor();
		copyFields(this, copy);
		return copy;
	}

}
