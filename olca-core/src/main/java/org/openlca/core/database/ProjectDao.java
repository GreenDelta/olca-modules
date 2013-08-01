package org.openlca.core.database;

import org.openlca.core.model.Project;
import org.openlca.core.model.descriptors.ProjectDescriptor;

public class ProjectDao extends
		CategorizedEntityDao<Project, ProjectDescriptor> {

	public ProjectDao(IDatabase database) {
		super(Project.class, ProjectDescriptor.class, database);
	}

}
