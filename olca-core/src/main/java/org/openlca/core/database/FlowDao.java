package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.FlowDescriptor;

public class FlowDao extends RootEntityDao<Flow, FlowDescriptor> {

	public FlowDao(IDatabase database) {
		super(Flow.class, FlowDescriptor.class, database);
	}

	@Override
	protected List<FlowDescriptor> queryDescriptors(
			String condition, List<Object> params) {
		var sql = """
					select
						d.id,
				   	d.ref_id,
				   	d.name,
				   	d.description,
				   	d.version,
				   	d.last_change,
				   	d.f_category,
				   	d.library,
				   	d.tags,
				   	d.flow_type,
				    d.f_location,
				    d.f_reference_flow_property	from
				""" + getEntityTable() + " d";
		if (condition != null) {
			sql += " " + condition;
		}

		var cons = descriptorConstructor();
		var list = new ArrayList<FlowDescriptor>();
		NativeSql.on(db).query(sql, params, r -> {
			var d = cons.get();
			d.id = r.getLong(1);
			d.refId = r.getString(2);
			d.name = r.getString(3);
			d.description = r.getString(4);
			d.version = r.getLong(5);
			d.lastChange = r.getLong(6);
			var catId = r.getLong(7);
			if (!r.wasNull()) {
				d.category = catId;
			}
			d.library = r.getString(8);
			d.tags = r.getString(9);

			var flowType = r.getString(10);
			if (flowType != null) {
				d.flowType = FlowType.valueOf(flowType);
			}

			var locId = r.getLong(11);
			if (!r.wasNull()) {
				d.location = locId;
			}

			var refProp = r.getLong(12);
			if (!r.wasNull()) {
				d.refFlowPropertyId = refProp;
			}

			list.add(d);
			return true;
		});
		return list;
	}

	public List<FlowDescriptor> getDescriptors(FlowType... flowTypes) {
		if (flowTypes == null || flowTypes.length == 0)
			return Collections.emptyList();

		var cond = new StringBuilder("where ");
		for (int i = 0; i < flowTypes.length; i++) {
			if (i > 0) {
				cond.append(" or ");
			}
			cond.append("d.flow_type = '")
					.append(flowTypes[i].name())
					.append('\'');
		}
		return queryDescriptors(cond.toString(), List.of());
	}

	/**
	 * Returns the processes where the given flow is an output.
	 */
	public Set<Long> getWhereOutput(long flowId) {
		return getProcessIdsWhereUsed(flowId, false);
	}

	/**
	 * Returns the processes where the given flow is an input.
	 */
	public Set<Long> getWhereInput(long flowId) {
		return getProcessIdsWhereUsed(flowId, true);
	}

	/**
	 * Get the IDs of all flows that are used in exchanges or LCIA factors.
	 */
	public Set<Long> getUsed() {
		Set<Long> ids = new HashSet<>();
		String[] tables = {"tbl_exchanges", "tbl_impact_factors"};
		for (String table : tables) {
			String query = "SELECT DISTINCT f_flow FROM " + table;
			try {
				NativeSql.on(db).query(query, (rs) -> {
					ids.add(rs.getLong(1));
					return true;
				});
			} catch (Exception e) {
				DatabaseException.logAndThrow(log, "failed to load used flows",
					e);
				return Collections.emptySet();
			}
		}
		return ids;
	}

	public Set<Long> getReplacementCandidates(long flowId, FlowType type) {
		Set<Long> ids = new HashSet<>();
		String query = "SELECT DISTINCT f_flow FROM tbl_flow_property_factors WHERE f_flow_property IN "
			+ "(SELECT f_flow_property FROM tbl_flow_property_factors WHERE f_flow = "
			+ flowId + ") "
			+ "AND f_flow IN (SELECT DISTINCT id FROM tbl_flows WHERE flow_type = '"
			+ type.name() + "')";
		try {
			NativeSql.on(db).query(query, (rs) -> {
				ids.add(rs.getLong("f_flow"));
				return true;
			});
			ids.remove(flowId);
			return ids;
		} catch (Exception e) {
			DatabaseException.logAndThrow(log,
				"failed to load replacement candidate flows for " + flowId,
				e);
			return Collections.emptySet();
		}
	}

	public void replaceExchangeFlowsWithoutProviders(long oldId, long newId) {
		replaceExchangeFlows(oldId, newId, true);
	}

	public void replaceExchangeFlows(long oldId, long newId) {
		replaceExchangeFlows(oldId, newId, false);
	}

	private void replaceExchangeFlows(long oldId, long newId,
		boolean excludeExchangesWithProviders) {
		try {
			String subquery = "SELECT id FROM tbl_flow_property_factors WHERE "
				+
				"f_flow_property = (SELECT f_flow_property FROM tbl_flow_property_factors WHERE id = tbl_exchanges.f_flow_property_factor) "
				+ "AND f_flow = " + newId;
			String query = "UPDATE tbl_exchanges SET f_flow = " + newId
				+ ", f_flow_property_factor = (" + subquery
				+ "), f_default_provider = null WHERE f_flow = " + oldId;
			if (excludeExchangesWithProviders) {
				query += " AND f_default_provider IS NULL";
			}
			NativeSql.on(db).runUpdate(query);
		} catch (Exception e) {
			DatabaseException.logAndThrow(log,
				"failed to replace flow " + oldId + " with " + newId, e);
		}
	}

	public void replaceImpactFlows(long oldId, long newId) {
		try {
			String subquery = "SELECT id FROM tbl_flow_property_factors WHERE "
				+ "f_flow_property = (SELECT f_flow_property FROM tbl_flow_property_factors WHERE id = tbl_impact_factors.f_flow_property_factor) "
				+ "AND f_flow = " + newId;
			String query = "UPDATE tbl_impact_factors SET f_flow = " + newId
				+ ", f_flow_property_factor = (" + subquery
				+ ") WHERE f_flow = " + oldId;
			NativeSql.on(db).runUpdate(query);
		} catch (Exception e) {
			DatabaseException.logAndThrow(log,
				"failed to replace flow " + oldId + " with " + newId, e);
		}
	}

	private Set<Long> getProcessIdsWhereUsed(long flowId, boolean input) {
		Set<Long> ids = new HashSet<>();
		String query = "SELECT f_owner FROM tbl_exchanges WHERE f_flow = "
			+ flowId + " AND is_input = "
			+ (input ? 1 : 0);
		try {
			NativeSql.on(db).query(query, (rs) -> {
				ids.add(rs.getLong("f_owner"));
				return true;
			});
			return ids;
		} catch (Exception e) {
			DatabaseException.logAndThrow(log,
				"failed to load processes for flow " + flowId, e);
			return Collections.emptySet();
		}
	}

	public boolean hasReferenceFactor(long id) {
		return hasReferenceFactor(Collections.singleton(id)).get(id);
	}

	public Map<Long, Boolean> hasReferenceFactor(Set<Long> ids) {
		if (ids == null || ids.isEmpty())
			return new HashMap<>();
		if (ids.size() > MAX_LIST_SIZE)
			return executeChunked2(ids, this::hasReferenceFactor);
		StringBuilder query = new StringBuilder();
		query.append("SELECT id, f_reference_flow_property FROM tbl_flows ");
		query.append("WHERE id IN " + asSqlList(ids));
		query.append(" AND f_reference_flow_property IN ");
		query.append(
			"(SELECT f_flow_property FROM tbl_flow_property_factors WHERE tbl_flows.id = f_flow)");
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
