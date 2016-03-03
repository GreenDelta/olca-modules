package org.openlca.core.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.model.NwSet;
import org.openlca.core.model.descriptors.NwSetDescriptor;

public class NwSetDao extends RootEntityDao<NwSet, NwSetDescriptor> {

	public NwSetDao(IDatabase database) {
		super(NwSet.class, NwSetDescriptor.class, database);
	}

	public List<NwSetDescriptor> getDescriptorsForMethod(long impactMethod) {
		String query = "select id from tbl_nw_sets where f_impact_method = "
				+ impactMethod;
		try {
			final Set<Long> ids = new HashSet<>();
			NativeSql.on(getDatabase()).query(query,
					new NativeSql.QueryResultHandler() {
						@Override
						public boolean nextResult(ResultSet result)
								throws SQLException {
							ids.add(result.getLong("id"));
							return true;
						}
					});
			return getDescriptors(ids);
		} catch (Exception e) {
			log.error("failed to get normalisation and weighting sets", e);
			return Collections.emptyList();
		}
	}

	@Override
	protected String[] getDescriptorFields() {
		return new String[] { "id", "ref_id", "name", "description", "version",
				"last_change", "weighted_score_unit" };
	}

	@Override
	protected NwSetDescriptor createDescriptor(Object[] queryResult) {
		NwSetDescriptor descriptor = super.createDescriptor(queryResult);
		if (descriptor != null)
			descriptor.setWeightedScoreUnit((String) queryResult[6]);
		return descriptor;
	}

}
