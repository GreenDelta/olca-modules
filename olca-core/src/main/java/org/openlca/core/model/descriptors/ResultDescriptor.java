package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class ResultDescriptor extends RootDescriptor {

	public ResultDescriptor() {
		this.type = ModelType.RESULT;
	}

	@Override
	public ResultDescriptor copy() {
		var copy = new ResultDescriptor();
		copyFields(this, copy);
		return copy;
	}

	public static Builder create() {
		return new Builder(new ResultDescriptor());
	}

	public static class Builder extends DescriptorBuilder<ResultDescriptor> {
		private Builder(ResultDescriptor descriptor) {
			super(descriptor);
		}
	}
}
