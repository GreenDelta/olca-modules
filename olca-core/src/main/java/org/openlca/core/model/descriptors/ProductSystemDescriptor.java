package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class ProductSystemDescriptor extends RootDescriptor {

	public ProductSystemDescriptor() {
		this.type = ModelType.PRODUCT_SYSTEM;
	}

	@Override
	public ProductSystemDescriptor copy() {
		var copy = new ProductSystemDescriptor();
		copyFields(this, copy);
		return copy;
	}

	public static Builder create() {
		return new Builder(new ProductSystemDescriptor());
	}

	public static class Builder extends DescriptorBuilder<ProductSystemDescriptor> {
		private Builder(ProductSystemDescriptor descriptor) {
			super(descriptor);
		}
	}

}
