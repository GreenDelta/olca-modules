package org.openlca.core.database;

import java.util.Collections;

import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public class ImpactCategoryDao extends BaseDao<ImpactCategory> {

	public ImpactCategoryDao(IDatabase database) {
		super(ImpactCategory.class, database);
	}

	public ImpactCategoryDescriptor getDescriptor(long id) {
		try {
			String jpql = "select c.name, c.description, c.referenceUnit"
					+ " from LCIACategory c where c.id = :id";
			Object[] result = Query.on(getDatabase()).getFirst(Object[].class,
					jpql, Collections.singletonMap("id", id));
			if (result == null)
				return null;
			ImpactCategoryDescriptor descriptor = new ImpactCategoryDescriptor();
			descriptor.setId(id);
			descriptor.setDescription((String) result[1]);
			descriptor.setName((String) result[0]);
			descriptor.setReferenceUnit((String) result[2]);
			return descriptor;
		} catch (Exception e) {
			log.error("Failed to load impact category descriptor", e);
			return null;
		}
	}

}
