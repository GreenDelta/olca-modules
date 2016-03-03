package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class CurrencyDescriptor extends CategorizedDescriptor {

	private static final long serialVersionUID = 3360761249255532527L;

	public CurrencyDescriptor() {
		setType(ModelType.CURRENCY);
	}
}
