package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

/**
 * The descriptor class for impact assessment categories.
 */
public class ImpactCategoryDescriptor extends BaseDescriptor {

	private static final long serialVersionUID = -8733726472954941608L;

	private String referenceUnit;

	public ImpactCategoryDescriptor() {
		setType(ModelType.IMPACT_CATEGORY);
	}

	public String getReferenceUnit() {
		return referenceUnit;
	}

	public void setReferenceUnit(String referenceUnit) {
		this.referenceUnit = referenceUnit;
	}

}
