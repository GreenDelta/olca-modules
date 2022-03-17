package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class SocialIndicatorDescriptor extends RootDescriptor {

	public SocialIndicatorDescriptor() {
		this.type = ModelType.SOCIAL_INDICATOR;
	}

	@Override
	public SocialIndicatorDescriptor copy() {
		var copy = new SocialIndicatorDescriptor();
		copyFields(this, copy);
		return copy;
	}

	public static Builder create() {
		return new Builder(new SocialIndicatorDescriptor());
	}

	public static class Builder extends DescriptorBuilder<SocialIndicatorDescriptor> {
		private Builder(SocialIndicatorDescriptor descriptor) {
			super(descriptor);
		}
	}
}
