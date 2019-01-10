package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

/**
 * The descriptor class for impact assessment categories.
 */
public class ImpactCategoryDescriptor extends BaseDescriptor {

	public String referenceUnit;

	public ImpactCategoryDescriptor() {
		this.type = ModelType.IMPACT_CATEGORY;
	}

}
