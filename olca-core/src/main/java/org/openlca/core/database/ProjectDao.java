package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Category;
import org.openlca.core.model.Project;
import org.openlca.core.model.descriptors.ProjectDescriptor;

public class ProjectDao extends BaseDao<Project> {

	public ProjectDao(EntityManagerFactory emf) {
		super(Project.class, emf);
	}

	public List<ProjectDescriptor> getDescriptors(Category category)
			throws Exception {
		String categoryId = category == null ? null : category.getId();
		String jpql = "select p.id, p.name, p.description from Project p "
				+ "where p.categoryId = :categoryId";
		List<Object[]> results = Query.on(getEntityFactory()).getAll(
				Object[].class, jpql,
				Collections.singletonMap("categoryId", categoryId));
		List<ProjectDescriptor> descriptors = new ArrayList<>();
		for (Object[] result : results) {
			ProjectDescriptor descriptor = new ProjectDescriptor();
			descriptor.setId((String) result[0]);
			descriptor.setName((String) result[1]);
			descriptor.setDescription((String) result[2]);
			descriptors.add(descriptor);
		}
		return descriptors;
	}

}
