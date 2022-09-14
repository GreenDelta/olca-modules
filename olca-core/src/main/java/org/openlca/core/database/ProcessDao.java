package org.openlca.core.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class ProcessDao extends RootEntityDao<Process, ProcessDescriptor> {

	public ProcessDao(IDatabase database) {
		super(Process.class, ProcessDescriptor.class, database);
	}

	@Override
	protected List<ProcessDescriptor> queryDescriptors(
			String condition, List<Object> params) {
		var sql = """
					select
						d.id,
						d.ref_id,
						d.name,
						d.version,
						d.last_change,
						d.f_category,
						d.library,
						d.tags,
						d.process_type,
						d.f_location,
						f.flow_type
				    from tbl_processes d
				    left join tbl_exchanges e on e.id = d.f_quantitative_reference
				    left  join tbl_flows f on e.f_flow = f.id
				""";
		if (condition != null) {
			sql += " " + condition;
		}

		var cons = descriptorConstructor();
		var list = new ArrayList<ProcessDescriptor>();
		NativeSql.on(db).query(sql, params, r -> {
			var d = cons.get();
			d.id = r.getLong(1);
			d.refId = r.getString(2);
			d.name = r.getString(3);
			d.version = r.getLong(4);
			d.lastChange = r.getLong(5);
			var catId = r.getLong(6);
			if (!r.wasNull()) {
				d.category = catId;
			}
			d.library = r.getString(7);
			d.tags = r.getString(8);

			var processType = r.getString(9);
			if (processType != null) {
				d.processType = ProcessType.valueOf(processType);
			}

			var locId = r.getLong(10);
			if (!r.wasNull()) {
				d.location = locId;
			}

			var flowType = r.getString(11);
			if (flowType != null) {
				d.flowType = FlowType.valueOf(flowType);
			}

			list.add(d);
			return true;
		});
		return list;
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
			NativeSql.on(db).query(query, (rs) -> {
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
			NativeSql.on(db).runUpdate(statement);
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
		NativeSql.on(db).query(query.toString(), (res) -> {
			result.put(res.getLong(1), res.getLong(2) != 0l);
			return true;
		});
		return result;
	}
}
