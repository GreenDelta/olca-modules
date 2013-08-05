package org.openlca.core.database;

import java.util.Collections;
import java.util.List;

import org.openlca.core.model.CostCategory;
import org.openlca.core.model.ProductCostEntry;

public class ProductCostEntryDao extends BaseDao<ProductCostEntry> {

	public ProductCostEntryDao(IDatabase database) {
		super(ProductCostEntry.class, database);
	}

	public List<ProductCostEntry> getAllForProduct(String exchangeId) {
		if (exchangeId == null)
			return Collections.emptyList();
		String jpql = "select e from ProductCostEntry e where "
				+ "e.exchangeId = :exchangeId";
		return getAll(jpql, Collections.singletonMap("exchangeId", exchangeId));
	}

	public List<ProductCostEntry> getAllForProcess(String processId) {
		if (processId == null)
			return Collections.emptyList();
		String jpql = "select e from ProductCostEntry e where "
				+ "e.processId = :processId";
		return getAll(jpql, Collections.singletonMap("processId", processId));
	}

	public List<ProductCostEntry> getAllForCategory(CostCategory category) {
		if (category == null)
			return Collections.emptyList();
		String jpql = "select e from ProductCostEntry e where "
				+ "e.costCategory = :category";
		return getAll(jpql, Collections.singletonMap("category", category));
	}

}
