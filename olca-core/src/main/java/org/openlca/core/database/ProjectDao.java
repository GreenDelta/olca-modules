package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Category;
import org.openlca.core.model.Project;
import org.openlca.core.model.descriptors.ProjectDescriptor;

import com.google.common.base.Optional;

public class ProjectDao extends RootEntityDao<Project> {

	public ProjectDao(EntityManagerFactory emf) {
		super(Project.class, emf);
	}

	public List<ProjectDescriptor> getDescriptors(Optional<Category> category) {
		String jpql = "select p.id, p.name, p.description from Project p ";
		Map<String, Category> params = null;
		if (category.isPresent()) {
			jpql += "where p.category = :category";
			params = Collections.singletonMap("category", category.get());
		} else {
			jpql += "where p.category is null";
			params = Collections.emptyMap();
		}
		return runDescriptorQuery(jpql, params);
	}

	private List<ProjectDescriptor> runDescriptorQuery(String jpql,
			Map<String, Category> params) {
		try {
			List<Object[]> results = Query.on(getEntityFactory()).getAll(
					Object[].class, jpql, params);
			return createDescriptors(results);
		} catch (Exception e) {
			log.error("failed to get product systems for category", e);
			return Collections.emptyList();
		}
	}

	private List<ProjectDescriptor> createDescriptors(List<Object[]> results) {
		List<ProjectDescriptor> descriptors = new ArrayList<>();
		for (Object[] result : results) {
			ProjectDescriptor descriptor = new ProjectDescriptor();
			descriptor.setId((Long) result[0]);
			descriptor.setName((String) result[1]);
			descriptor.setDescription((String) result[2]);
			descriptors.add(descriptor);
		}
		return descriptors;
	}

}
