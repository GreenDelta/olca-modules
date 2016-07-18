package org.openlca.core.database;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.model.DQSystem;
import org.openlca.core.model.descriptors.DQSystemDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DQSystemDao extends CategorizedEntityDao<DQSystem, DQSystemDescriptor> {

	private final static Logger log = LoggerFactory.getLogger(DQSystemDao.class);

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
		try {
			NativeSql.on(database).query(query, (rs) -> {
				ids.add(rs.getLong(field));
				return true;
			});
		} catch (SQLException e) {
			log.error("Error loading data quality systems (" + field + ") for product system " + productSystemId, e);
		}
		return getDescriptors(ids);
	}
}
