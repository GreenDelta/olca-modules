package org.openlca.core.model.descriptors;

import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;

public class FlowDescriptor extends CategorizedDescriptor {

	public Long location;
	public FlowType flowType;
	public long refFlowPropertyId;

	public FlowDescriptor() {
		this.type = ModelType.FLOW;
	}

	@Override
	public FlowDescriptor copy() {
		var copy = new FlowDescriptor();
		copyFields(this, copy);
		copy.location = location;
		copy.flowType = flowType;
		copy.refFlowPropertyId = refFlowPropertyId;
		return copy;
	}

}
