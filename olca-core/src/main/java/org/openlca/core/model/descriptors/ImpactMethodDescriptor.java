package org.openlca.core.model.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.ModelType;

/**
 * The descriptor class for impact assessment methods.
 */
public class ImpactMethodDescriptor extends BaseDescriptor {

	private List<ImpactCategoryDescriptor> categories = new ArrayList<>();

	public ImpactMethodDescriptor() {
		setType(ModelType.IMPACT_METHOD);
	}

	public List<ImpactCategoryDescriptor> getImpactCategories() {
		return categories;
	}

}
