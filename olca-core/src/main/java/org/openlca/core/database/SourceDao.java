package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Category;
import org.openlca.core.model.Source;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.SourceDescriptor;

public class SourceDao extends BaseDao<Source> implements
		IRootEntityDao<Source> {

	public SourceDao(EntityManagerFactory emf) {
		super(Source.class, emf);
	}

	public List<SourceDescriptor> getDescriptors(Category category) {
		log.trace("get sources for category {}", category);
		String jpql = "select s.id, s.name, s.description from Source s "
				+ "where s.category = :category";
		try {
			List<Object[]> results = Query.on(getEntityFactory()).getAll(
					Object[].class, jpql,
					Collections.singletonMap("category", category));
			return createDescriptors(results);
		} catch (Exception e) {
			log.error("failed to get sources for category " + category, e);
			return Collections.emptyList();
		}
	}

	private List<SourceDescriptor> createDescriptors(List<Object[]> results) {
		List<SourceDescriptor> descriptors = new ArrayList<>();
		for (Object[] result : results) {
			SourceDescriptor descriptor = new SourceDescriptor();
			descriptor.setId((String) result[0]);
			descriptor.setName((String) result[1]);
			descriptor.setDescription((String) result[2]);
			descriptors.add(descriptor);
		}
		return descriptors;
	}

	public List<BaseDescriptor> whereUsed(Source source) {
		return new SourceUseSearch(getEntityFactory()).findUses(source);
	}

}
