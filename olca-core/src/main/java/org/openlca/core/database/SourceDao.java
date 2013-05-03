package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Category;
import org.openlca.core.model.Source;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.SourceDescriptor;

public class SourceDao extends BaseDao<Source> {

	public SourceDao(EntityManagerFactory emf) {
		super(Source.class, emf);
	}

	public List<SourceDescriptor> getDescriptors(Category category)
			throws Exception {
		String categoryId = category == null ? null : category.getId();
		String jpql = "select s.id, s.name, s.description from Source s "
				+ "where s.categoryId = :categoryId";
		List<Object[]> results = Query.on(getEntityFactory()).getAll(
				Object[].class, jpql,
				Collections.singletonMap("categoryId", categoryId));
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
