package org.openlca.core.database;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.CostCategory;
import org.openlca.core.model.ProductCostEntry;

public class ProductCostEntryDao extends BaseDao<ProductCostEntry> {

	public ProductCostEntryDao(EntityManagerFactory factory) {
		super(ProductCostEntry.class, factory);
	}

	public List<ProductCostEntry> getAllForProduct(String exchangeId)
			throws Exception {
		if (exchangeId == null)
			return Collections.emptyList();
		String jpql = "select e from ProductCostEntry e where "
				+ "e.exchangeId = :exchangeId";
		return getAll(jpql, Collections.singletonMap("exchangeId", exchangeId));
	}

	public List<ProductCostEntry> getAllForProcess(String processId)
			throws Exception {
		if (processId == null)
			return Collections.emptyList();
		String jpql = "select e from ProductCostEntry e where "
				+ "e.processId = :processId";
		return getAll(jpql, Collections.singletonMap("processId", processId));
	}

	public List<ProductCostEntry> getAllForCategory(CostCategory category)
			throws Exception {
		if (category == null)
			return Collections.emptyList();
		String jpql = "select e from ProductCostEntry e where "
				+ "e.costCategory = :category";
		return getAll(jpql, Collections.singletonMap("category", category));
	}

}
