package org.openlca.core.database;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.FlowDescriptor;

public class FlowDao extends CategorizedEntityDao<Flow, FlowDescriptor> {

	public FlowDao(IDatabase database) {
		super(Flow.class, FlowDescriptor.class, database);
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
				"flow_type",
				"f_location",
				"f_reference_flow_property"
		};
	}

	@Override
	protected FlowDescriptor createDescriptor(Object[] queryResult) {
		if (queryResult == null)
			return null;
		var d = super.createDescriptor(queryResult);
		if (queryResult[8] instanceof String) {
			d.flowType = FlowType.valueOf((String) queryResult[8]);
		}
		d.location = (Long) queryResult[9];
		Long refProp = (Long) queryResult[10];
		if (refProp != null) {
			d.refFlowPropertyId = refProp;
		}
		return d;
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
		String[] tables = { "tbl_exchanges", "tbl_impact_factors" };
		for (String table : tables) {
			String query = "SELECT DISTINCT f_flow FROM " + table;
			try {
				NativeSql.on(database).query(query, (rs) -> {
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
			NativeSql.on(database).query(query, (rs) -> {
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
			NativeSql.on(database).runUpdate(query);
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
			NativeSql.on(database).runUpdate(query);
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
			NativeSql.on(database).query(query, (rs) -> {
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
		NativeSql.on(database).query(query.toString(), (res) -> {
			result.put(res.getLong(1), res.getLong(2) != 0l);
			return true;
		});
		return result;
	}

}
