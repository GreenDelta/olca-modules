package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessType;

public class ProcessDescriptor extends RootDescriptor {

	public ProcessType processType;
	public boolean infrastructureProcess;
	public Long location;
	public Long quantitativeReference; // TODO: is this field used?, why?

	public ProcessDescriptor() {
		this.type = ModelType.PROCESS;
	}

	@Override
	public ProcessDescriptor copy() {
		var copy = new ProcessDescriptor();
		copyFields(this, copy);
		copy.processType = processType;
		copy.infrastructureProcess = infrastructureProcess;
		copy.location = location;
		copy.quantitativeReference = quantitativeReference;
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

		public Builder infrastructureProcess(boolean infrastructureProcess) {
			descriptor.infrastructureProcess = infrastructureProcess;
			return this;
		}

		public Builder location(Long location) {
			descriptor.location = location;
			return this;
		}

		public Builder quantitativeReference(Long quantitativeReference) {
			descriptor.quantitativeReference = quantitativeReference;
			return this;
		}
	}
}
