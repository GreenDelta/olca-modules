package org.openlca.core.model.descriptors;

public class NwSetDescriptor extends Descriptor {

	public String weightedScoreUnit;

	@Override
	public NwSetDescriptor copy() {
		var copy = new NwSetDescriptor();
		copyFields(this, copy);
		copy.weightedScoreUnit = weightedScoreUnit;
		return copy;
	}

	public static Builder create() {
		return new Builder(new NwSetDescriptor());
	}

	public static class Builder extends DescriptorBuilder<NwSetDescriptor> {
		private Builder(NwSetDescriptor descriptor) {
			super(descriptor);
		}
	}
}
