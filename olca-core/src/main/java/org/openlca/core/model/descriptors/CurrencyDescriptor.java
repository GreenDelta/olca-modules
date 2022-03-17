package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class CurrencyDescriptor extends RootDescriptor {

	public CurrencyDescriptor() {
		this.type = ModelType.CURRENCY;
	}

	@Override
	public CurrencyDescriptor copy() {
		var copy = new CurrencyDescriptor();
		copyFields(this, copy);
		return copy;
	}

	public static Builder create() {
		return new Builder(new CurrencyDescriptor());
	}

	public static class Builder extends DescriptorBuilder<CurrencyDescriptor> {
		private Builder(CurrencyDescriptor descriptor) {
			super(descriptor);
		}
	}
}
