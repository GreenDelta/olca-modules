package org.openlca.core.database;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;

public class ProductSystemDao extends
        RootEntityDao<ProductSystem, ProductSystemDescriptor> {

	public ProductSystemDao(IDatabase database) {
		super(ProductSystem.class, ProductSystemDescriptor.class, database);
	}

	public boolean hasReferenceProcess(long id) {
		return hasReferenceProcess(Collections.singleton(id)).get(id);
	}

	public Map<Long, Boolean> hasReferenceProcess(Set<Long> ids) {
		Map<Long, Boolean> result = new HashMap<>();
		for (long id : ids)
			result.put(id, false);
		var query = "SELECT id, f_reference_process FROM tbl_product_systems " +
								"WHERE id IN " + asSqlList(ids) +
								"AND f_reference_process IN " +
								"(SELECT f_process FROM tbl_product_system_processes " +
								"WHERE f_process = f_reference_process)";
		NativeSql.on(db).query(query, (res) -> {
			result.put(res.getLong(1), res.getLong(2) != 0L);
			return true;
		});
		return result;
	}

}
