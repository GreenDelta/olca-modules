package org.openlca.core.database;

import org.openlca.core.model.Project;

public class ProjectDao extends CategorizedEnitityDao<Project> {

	public ProjectDao(IDatabase database) {
		super(Project.class, database);
	}

}
