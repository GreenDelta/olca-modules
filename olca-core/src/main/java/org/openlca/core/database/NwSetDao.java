package org.openlca.core.database;

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
	protected String[] getDescriptorFields() {
		return new String[] {
				"id",
				"ref_id",
				"name",
				"description",
				"weighted_score_unit",
		};
	}

	@Override
	protected NwSetDescriptor createDescriptor(Object[] record) {
		var d = super.createDescriptor(record);
		d.weightedScoreUnit = (String) record[4];
		return d;
	}

}
