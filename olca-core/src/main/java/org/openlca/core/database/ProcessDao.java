package org.openlca.core.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class ProcessDao extends CategorizedEntityDao<Process, ProcessDescriptor> {

	public ProcessDao(IDatabase database) {
		super(Process.class, ProcessDescriptor.class, database);
	}

	@Override
	protected String[] getDescriptorFields() {
		return new String[] {
				"id",
				"ref_id",
				"name",
				"description",
				"version",
				"last_change",
				"f_category",
				"library",
				"process_type",
				"infrastructure_process",
				"f_location",
				"f_quantitative_reference",
		};
	}

	@Override
	protected ProcessDescriptor createDescriptor(Object[] queryResult) {
		if (queryResult == null)
			return null;
		var d = super.createDescriptor(queryResult);
		if (queryResult[8] instanceof String) {
			d.processType = ProcessType.valueOf((String) queryResult[8]);
		}
		if (queryResult[9] instanceof Integer) {
			d.infrastructureProcess = (Integer) queryResult[9] == 1;
		}
		d.location = (Long) queryResult[10];
		d.quantitativeReference = (Long) queryResult[11];
		return d;
	}

	public List<FlowDescriptor> getTechnologyInputs(ProcessDescriptor descriptor) {
		Set<Long> flowIds = getTechnologies(descriptor, true);
		return loadFlowDescriptors(flowIds);
	}

	public List<FlowDescriptor> getTechnologyOutputs(
			ProcessDescriptor descriptor) {
		Set<Long> flowIds = getTechnologies(descriptor, false);
		return loadFlowDescriptors(flowIds);
	}

	public Set<Long> getUsed() {
		Set<Long> ids = new HashSet<>();
		String query = "SELECT DISTINCT f_default_provider FROM tbl_exchanges";
		try {
			NativeSql.on(database).query(query, (rs) -> {
				ids.add(rs.getLong("f_default_provider"));
				return true;
			});
			return ids;
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "failed to load used providers", e);
			return Collections.emptySet();
		}
	}

	public void replace(long oldId, long productId, Long newId) {
		String statement = "UPDATE tbl_exchanges SET f_default_provider = " + newId + " "
				+ "WHERE f_default_provider = " + oldId + " AND f_flow = " + productId;
		try {
			NativeSql.on(database).runUpdate(statement);
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "failed to replace provider " + oldId + " for product " + productId
					+ " with " + newId, e);
		}
	}

	private Set<Long> getTechnologies(ProcessDescriptor descriptor,
			boolean input) {
		if (descriptor == null)
			return Collections.emptySet();
		String sql = "select f_flow from tbl_exchanges where f_owner = "
				+ descriptor.id + " and is_input = " + (input ? 1 : 0);
		Set<Long> ids = new HashSet<>();
		try (Connection con = getDatabase().createConnection();
				Statement s = con.createStatement();
				ResultSet rs = s.executeQuery(sql)) {
			while (rs.next())
				ids.add(rs.getLong("f_flow"));
			return ids;
		} catch (SQLException e) {
			log.error("Error loading technologies", e);
			return Collections.emptySet();
		}
	}

	private List<FlowDescriptor> loadFlowDescriptors(Set<Long> flowIds) {
		if (flowIds == null || flowIds.isEmpty())
			return Collections.emptyList();
		FlowDao dao = new FlowDao(getDatabase());
		return dao.getDescriptors(flowIds);
	}

	public boolean hasQuantitativeReference(long id) {
		return hasQuantitativeReference(Collections.singleton(id)).get(id);
	}

	public Map<Long, Boolean> hasQuantitativeReference(Set<Long> ids) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT id, f_quantitative_reference FROM tbl_processes ");
		query.append("WHERE id IN " + asSqlList(ids));
		query.append(" AND f_quantitative_reference IN ");
		query.append("(SELECT id FROM tbl_exchanges WHERE id = f_quantitative_reference)");
		Map<Long, Boolean> result = new HashMap<>();
		for (long id : ids)
			result.put(id, false);
		NativeSql.on(database).query(query.toString(), (res) -> {
			result.put(res.getLong(1), res.getLong(2) != 0l);
			return true;
		});
		return result;
	}
}
