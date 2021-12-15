package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class ProductSystemDescriptor extends CategorizedDescriptor {

	public ProductSystemDescriptor() {
		this.type = ModelType.PRODUCT_SYSTEM;
	}

	@Override
	public ProductSystemDescriptor copy() {
		var copy = new ProductSystemDescriptor();
		copyFields(this, copy);
		return copy;
	}
}
