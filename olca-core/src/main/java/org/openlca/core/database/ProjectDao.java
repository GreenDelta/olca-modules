package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Category;
import org.openlca.core.model.Project;
import org.openlca.core.model.descriptors.ProjectDescriptor;

public class ProjectDao extends BaseDao<Project> implements
		IRootEntityDao<Project> {

	public ProjectDao(EntityManagerFactory emf) {
		super(Project.class, emf);
	}

	public List<ProjectDescriptor> getDescriptors(Category category) {
		log.trace("get projects for category {}", category);
		String jpql = "select p.id, p.name, p.description from Project p "
				+ "where p.category = :category";
		try {
			List<Object[]> results = Query.on(getEntityFactory()).getAll(
					Object[].class, jpql,
					Collections.singletonMap("category", category));
			return createDescriptors(results);
		} catch (Exception e) {
			log.error("failed to get product systems for category " + category,
					e);
			return Collections.emptyList();
		}
	}

	private List<ProjectDescriptor> createDescriptors(List<Object[]> results) {
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
