package org.openlca.core.model.descriptors;

import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessType;

public class ProcessDescriptor extends RootDescriptor {

	public ProcessType processType;

	/**
	 * The flow type of the quantitative reference, if defined.
	 */
	public FlowType flowType;

	public Long location;

	public ProcessDescriptor() {
		this.type = ModelType.PROCESS;
	}

	@Override
	public ProcessDescriptor copy() {
		var copy = new ProcessDescriptor();
		copyFields(this, copy);
		copy.processType = processType;
		copy.flowType = flowType;
		copy.location = location;
		return copy;
	}

	public static Builder create() {
		return new Builder(new ProcessDescriptor());
	}

	public static class Builder extends DescriptorBuilder<ProcessDescriptor> {

		private Builder(ProcessDescriptor descriptor) {
			super(descriptor);
		}

		public Builder processType(ProcessType processType) {
			descriptor.processType = processType;
			return this;
		}

		public Builder flowType(FlowType flowType) {
			descriptor.flowType = flowType;
			return this;
		}

		public Builder location(Long location) {
			descriptor.location = location;
			return this;
		}
	}
}
