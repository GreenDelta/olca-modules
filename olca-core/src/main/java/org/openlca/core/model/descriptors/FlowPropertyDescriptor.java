package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class FlowPropertyDescriptor extends CategorizedDescriptor {

	private static final long serialVersionUID = 1880752468793267637L;

	public FlowPropertyDescriptor() {
		setType(ModelType.FLOW_PROPERTY);
	}
}
