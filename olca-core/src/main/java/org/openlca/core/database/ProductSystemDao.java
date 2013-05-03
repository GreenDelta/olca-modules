package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Category;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;

public class ProductSystemDao extends BaseDao<ProductSystem> {

	public ProductSystemDao(EntityManagerFactory emf) {
		super(ProductSystem.class, emf);
	}

	public List<ProductSystemDescriptor> getDescriptors(Category category)
			throws Exception {
		String categoryId = category == null ? null : category.getId();
		String jpql = "select s.id, s.name, s.description from ProductSystem s "
				+ "where s.categoryId = :categoryId";
		List<Object[]> results = Query.on(getEntityFactory()).getAll(
				Object[].class, jpql,
				Collections.singletonMap("categoryId", categoryId));
		List<ProductSystemDescriptor> descriptors = new ArrayList<>();
		for (Object[] result : results) {
			ProductSystemDescriptor descriptor = new ProductSystemDescriptor();
			descriptor.setId((String) result[0]);
			descriptor.setName((String) result[1]);
			descriptor.setDescription((String) result[2]);
			descriptors.add(descriptor);
		}
		return descriptors;
	}

}
