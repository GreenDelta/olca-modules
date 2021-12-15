package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

/**
 * The descriptor class for impact assessment categories.
 */
public class ImpactDescriptor extends CategorizedDescriptor {

	public String referenceUnit;

	public ImpactDescriptor() {
		this.type = ModelType.IMPACT_CATEGORY;
	}

	@Override
	public ImpactDescriptor copy() {
		var copy = new ImpactDescriptor();
		copyFields(this, copy);
		copy.referenceUnit = referenceUnit;
		return copy;
	}

}
