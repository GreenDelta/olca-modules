package org.openlca.core.database;

import java.util.Collections;
import java.util.List;

import org.openlca.core.model.CostCategory;
import org.openlca.core.model.ProcessCostEntry;

public class ProductCostEntryDao extends BaseDao<ProcessCostEntry> {

	public ProductCostEntryDao(IDatabase database) {
		super(ProcessCostEntry.class, database);
	}

	public List<ProcessCostEntry> getAllForProduct(String exchangeId) {
		if (exchangeId == null)
			return Collections.emptyList();
		String jpql = "select e from ProductCostEntry e where "
				+ "e.exchangeId = :exchangeId";
		return getAll(jpql, Collections.singletonMap("exchangeId", exchangeId));
	}

	public List<ProcessCostEntry> getAllForProcess(String processId) {
		if (processId == null)
			return Collections.emptyList();
		String jpql = "select e from ProductCostEntry e where "
				+ "e.processId = :processId";
		return getAll(jpql, Collections.singletonMap("processId", processId));
	}

	public List<ProcessCostEntry> getAllForCategory(CostCategory category) {
		if (category == null)
			return Collections.emptyList();
		String jpql = "select e from ProductCostEntry e where "
				+ "e.costCategory = :category";
		return getAll(jpql, Collections.singletonMap("category", category));
	}

}
