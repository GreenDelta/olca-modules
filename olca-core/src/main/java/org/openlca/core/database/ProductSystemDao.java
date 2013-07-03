package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Category;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;

import com.google.common.base.Optional;

public class ProductSystemDao extends RootEntityDao<ProductSystem> {

	public ProductSystemDao(EntityManagerFactory emf) {
		super(ProductSystem.class, emf);
	}

	public List<ProductSystemDescriptor> getDescriptors(
			Optional<Category> category) {
		String jpql = "select s.id, s.name, s.description from ProductSystem s ";
		Map<String, Category> params = null;
		if (category.isPresent()) {
			params = Collections.singletonMap("category", category.get());
			jpql += "where s.category = :category";
		} else {
			params = Collections.emptyMap();
			jpql += "where s.category is null";
		}
		return runDescriptorQuery(jpql, params);
	}

	private List<ProductSystemDescriptor> runDescriptorQuery(String jpql,
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

	private List<ProductSystemDescriptor> createDescriptors(
			List<Object[]> results) {
		List<ProductSystemDescriptor> descriptors = new ArrayList<>();
		for (Object[] result : results) {
			ProductSystemDescriptor descriptor = new ProductSystemDescriptor();
			descriptor.setId((Long) result[0]);
			descriptor.setName((String) result[1]);
			descriptor.setDescription((String) result[2]);
			descriptors.add(descriptor);
		}
		return descriptors;
	}

}
