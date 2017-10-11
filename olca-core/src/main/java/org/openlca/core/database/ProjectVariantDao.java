package org.openlca.core.database;

import org.openlca.core.model.ProjectVariant;

public class ProjectVariantDao extends BaseDao<ProjectVariant> {

	public ProjectVariantDao(IDatabase database) {
		super(ProjectVariant.class, database);
	}
}
