package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.openlca.core.model.NwSet;
import org.openlca.core.model.descriptors.NwSetDescriptor;

public class NwSetDao extends RefEntityDao<NwSet, NwSetDescriptor> {

	public NwSetDao(IDatabase database) {
		super(NwSet.class, NwSetDescriptor.class, database);
	}

	public List<NwSet> allOfMethod(long methodId) {
		String query = "select id from tbl_nw_sets where f_impact_method = "
				+ methodId;
		try {
			var ids = new HashSet<Long>();
			NativeSql.on(getDatabase()).query(query, result -> {
				ids.add(result.getLong("id"));
				return true;
			});
			return getForIds(ids);
		} catch (Exception e) {
			log.error("failed to get normalisation and weighting sets", e);
			return Collections.emptyList();
		}
	}

	@Override
	protected List<NwSetDescriptor> queryDescriptors(String condition, List<Object> params) {
		var sql = """
						select
							d.id,
							d.ref_id,
							d.name,
							d.description,
							d.weighted_score_unit from
				""" + getEntityTable() + " d";
		if (condition != null) {
			sql += " " + condition;
		}
		var cons = descriptorConstructor();
		var list = new ArrayList<NwSetDescriptor>();
		NativeSql.on(db).query(sql, params, r -> {
			var d = cons.get();
			d.id = r.getLong(1);
			d.refId = r.getString(2);
			d.name = r.getString(3);
			d.description = r.getString(4);
			d.weightedScoreUnit = r.getString(5);
			list.add(d);
			return true;
		});
		return list;
	}
}
