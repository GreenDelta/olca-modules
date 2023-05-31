package org.openlca.core.database.descriptors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.model.descriptors.ProjectDescriptor;

public class ProjectDescriptors
		extends RootDescriptorReader<ProjectDescriptor> {

	private ProjectDescriptors(IDatabase db) {
		super(new ProjectDao(db));
	}

	public static ProjectDescriptors of(IDatabase db) {
		return new ProjectDescriptors(db);
	}
}
