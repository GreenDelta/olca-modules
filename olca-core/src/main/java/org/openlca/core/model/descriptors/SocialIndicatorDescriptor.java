package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class SocialIndicatorDescriptor extends CategorizedDescriptor {

	public SocialIndicatorDescriptor() {
		this.type = ModelType.SOCIAL_INDICATOR;
	}

	@Override
	public SocialIndicatorDescriptor copy() {
		var copy = new SocialIndicatorDescriptor();
		copyFields(this, copy);
		return copy;
	}

}
