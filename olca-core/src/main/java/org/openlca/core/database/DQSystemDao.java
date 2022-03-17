package org.openlca.core.database;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.model.DQSystem;
import org.openlca.core.model.descriptors.DQSystemDescriptor;

public class DQSystemDao extends RootEntityDao<DQSystem, DQSystemDescriptor> {

	public DQSystemDao(IDatabase database) {
		super(DQSystem.class, DQSystemDescriptor.class, database);
	}

	public List<DQSystemDescriptor> getProcessDqSystems(long productSystemId) {
		return getDqSystems("f_dq_system", productSystemId);
	}

	public List<DQSystemDescriptor> getExchangeDqSystems(long productSystemId) {
		return getDqSystems("f_exchange_dq_system", productSystemId);
	}

	private List<DQSystemDescriptor> getDqSystems(String field, long productSystemId) {
		String query = "SELECT DISTINCT " + field + " FROM tbl_processes ";
		query += "INNER JOIN tbl_product_system_processes ON tbl_product_system_processes.f_process = tbl_processes.id ";
		query += "WHERE f_product_system = " + productSystemId;
		Set<Long> ids = new HashSet<>();
		NativeSql.on(db).query(query, (rs) -> {
			ids.add(rs.getLong(field));
			return true;
		});
		return getDescriptors(ids);
	}
}
