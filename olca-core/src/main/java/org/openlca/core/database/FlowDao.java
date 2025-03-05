package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
				   	d.version,
				   	d.last_change,
				   	d.f_category,
				   	d.data_package,
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
			d.version = r.getLong(4);
			d.lastChange = r.getLong(5);
			var catId = r.getLong(6);
			if (!r.wasNull()) {
				d.category = catId;
			}
			d.dataPackage = r.getString(7);
			d.tags = r.getString(8);

			d.flowType = NativeSql.enumItemOf(
					FlowType.class, r.getString(9));

			var locId = r.getLong(10);
			if (!r.wasNull()) {
				d.location = locId;
			}

			var refProp = r.getLong(11);
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
}
