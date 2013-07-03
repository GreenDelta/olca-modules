package org.openlca.core.database;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Project;

public class ProjectDao extends CategorizedEnitityDao<Project> {

	public ProjectDao(EntityManagerFactory emf) {
		super(Project.class, emf);
	}

}
