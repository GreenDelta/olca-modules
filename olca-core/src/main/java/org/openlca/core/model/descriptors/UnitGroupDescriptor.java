package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class UnitGroupDescriptor extends CategorizedDescriptor {

	public UnitGroupDescriptor() {
		this.type = ModelType.UNIT_GROUP;
	}

	@Override
	public UnitGroupDescriptor copy() {
		var copy = new UnitGroupDescriptor();
		copyFields(this, copy);
		return copy;
	}

}
