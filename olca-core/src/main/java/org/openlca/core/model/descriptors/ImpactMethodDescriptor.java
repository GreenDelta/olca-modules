package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

/**
 * The descriptor class for impact assessment methods.
 */
public class ImpactMethodDescriptor extends CategorizedDescriptor {

	public ImpactMethodDescriptor() {
		this.type = ModelType.IMPACT_METHOD;
	}

	@Override
	public ImpactMethodDescriptor copy() {
		var copy = new ImpactMethodDescriptor();
		copyFields(this, copy);
		return copy;
	}

}
