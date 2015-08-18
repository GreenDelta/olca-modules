package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class SocialIndicatorDescriptor extends CategorizedDescriptor {

	private static final long serialVersionUID = 1010709338032641124L;

	@Override
	public ModelType getModelType() {
		return ModelType.SOCIAL_INDICATOR;
	}

}
