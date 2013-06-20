package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Category;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;

public class ProductSystemDao extends BaseDao<ProductSystem> implements
		IRootEntityDao<ProductSystem> {

	public ProductSystemDao(EntityManagerFactory emf) {
		super(ProductSystem.class, emf);
	}

	public List<ProductSystemDescriptor> getDescriptors(Category category) {
		log.trace("get product systems for category {}", category);
		String jpql = "select s.id, s.name, s.description from ProductSystem s "
				+ "where s.category = :category";
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

	private List<ProductSystemDescriptor> createDescriptors(
			List<Object[]> results) {
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
