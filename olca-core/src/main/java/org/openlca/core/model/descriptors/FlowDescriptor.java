package org.openlca.core.model.descriptors;

import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;

public class FlowDescriptor extends CategorizedDescriptor {

	private static final long serialVersionUID = 4292185203406513488L;

	private Long location;
	private FlowType flowType;
	private long refFlowPropertyId;

	public FlowDescriptor() {
		setType(ModelType.FLOW);
	}

	public Long getLocation() {
		return location;
	}

	public void setLocation(Long location) {
		this.location = location;
	}

	public FlowType getFlowType() {
		return flowType;
	}

	public void setFlowType(FlowType flowType) {
		this.flowType = flowType;
	}

	public long getRefFlowPropertyId() {
		return refFlowPropertyId;
	}

	public void setRefFlowPropertyId(long refFlowPropertyId) {
		this.refFlowPropertyId = refFlowPropertyId;
	}

}
