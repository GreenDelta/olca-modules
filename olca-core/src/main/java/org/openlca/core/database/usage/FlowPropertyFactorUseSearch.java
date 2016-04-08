package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

/**
 * Searches for the usage of flow property factors of a given flow in other
 * entities of the database.
 */
public class FlowPropertyFactorUseSearch {

	private IDatabase database;
	private Flow flow;

	public FlowPropertyFactorUseSearch(Flow flow, IDatabase database) {
		this.flow = flow;
		this.database = database;
	}

	public List<CategorizedDescriptor> findUses(FlowPropertyFactor factor) {
		if (flow == null || factor == null || database == null)
			return Collections.emptyList();
		List<CategorizedDescriptor> results = new ArrayList<>();
		// only exchange and impact factor are relevant, because all others can
		// only refer to units that are used in one of them
		Set<Long> categoryIds = Search.on(database).queryForIds(
				getImpactCategoryQuery(factor));
		results.addAll(Search.on(database).queryFor(ModelType.IMPACT_METHOD,
				"f_impact_method", "tbl_impact_categories", categoryIds, "id"));
		results.addAll(Search.on(database).queryFor(ModelType.PROCESS,
				getProcessQuery(factor)));
		return results;
	}

	private String getImpactCategoryQuery(FlowPropertyFactor factor) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT DISTINCT f_impact_category FROM tbl_impact_factors");
		query.append(" WHERE f_flow = " + flow.getId());
		query.append(" AND f_flow_property_factor = " + factor.getId());
		return query.toString();
	}

	private String getProcessQuery(FlowPropertyFactor factor) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT DISTINCT f_owner FROM tbl_exchanges");
		query.append(" WHERE f_flow = " + flow.getId());
		query.append(" AND f_flow_property_factor = " + factor.getId());
		return query.toString();
	}

}
