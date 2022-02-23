package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class ProjectDescriptor extends RootDescriptor {

	public ProjectDescriptor() {
		this.type = ModelType.PROJECT;
	}

	@Override
	public ProjectDescriptor copy() {
		var copy = new ProjectDescriptor();
		copyFields(this, copy);
		return copy;
	}

	public static Builder create() {
		return new Builder(new ProjectDescriptor());
	}

	public static class Builder extends DescriptorBuilder<ProjectDescriptor> {
		private Builder(ProjectDescriptor descriptor) {
			super(descriptor);
		}
	}
}
