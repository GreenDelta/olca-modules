package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class CurrencyDescriptor extends CategorizedDescriptor {

	public CurrencyDescriptor() {
		this.type = ModelType.CURRENCY;
	}

	@Override
	public CurrencyDescriptor copy() {
		var copy = new CurrencyDescriptor();
		copyFields(this, copy);
		return copy;
	}

}
