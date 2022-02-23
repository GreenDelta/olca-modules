package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class ActorDescriptor extends RootDescriptor {

	public ActorDescriptor() {
		this.type = ModelType.ACTOR;
	}

	@Override
	public ActorDescriptor copy() {
		var copy = new ActorDescriptor();
		copyFields(this, copy);
		return copy;
	}

	public static Builder create() {
		return new Builder(new ActorDescriptor());
	}

	public static class Builder extends DescriptorBuilder<ActorDescriptor> {
		private Builder(ActorDescriptor descriptor) {
			super(descriptor);
		}
	}
}
