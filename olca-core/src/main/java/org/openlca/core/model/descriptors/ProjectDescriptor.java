package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class ProjectDescriptor extends CategorizedDescriptor {

	public ProjectDescriptor() {
		this.type = ModelType.PROJECT;
	}

	@Override
	public ProjectDescriptor copy() {
		var copy = new ProjectDescriptor();
		copyFields(this, copy);
		return copy;
	}

}
