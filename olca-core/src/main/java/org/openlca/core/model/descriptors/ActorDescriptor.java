package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class ActorDescriptor extends CategorizedDescriptor {

	public ActorDescriptor() {
		this.type = ModelType.ACTOR;
	}

	@Override
	public ActorDescriptor copy() {
		var copy = new ActorDescriptor();
		copyFields(this, copy);
		return copy;
	}
}
