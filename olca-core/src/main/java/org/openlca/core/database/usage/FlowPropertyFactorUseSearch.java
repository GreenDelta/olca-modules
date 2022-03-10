package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;

/**
 * Searches for the usage of flow property factors of a given flow in other
 * entities of the database.
 */
public record FlowPropertyFactorUseSearch(Flow flow, IDatabase db) {

	public List<RootDescriptor> findUses(FlowPropertyFactor factor) {
		if (flow == null || factor == null || db == null)
			return Collections.emptyList();
		List<RootDescriptor> results = new ArrayList<>();
		// only exchange and impact factor are relevant, because all others can
		// only refer to units that are used in one of them
		results.addAll(Search.on(db).queryFor(ModelType.IMPACT_CATEGORY,
			getImpactCategoryQuery(factor)));
		results.addAll(Search.on(db).queryFor(ModelType.PROCESS,
			getProcessQuery(factor)));
		return results;
	}

	private String getImpactCategoryQuery(FlowPropertyFactor factor) {
		return "SELECT DISTINCT f_impact_category FROM tbl_impact_factors" +
			" WHERE f_flow = " + flow.id +
			" AND f_flow_property_factor = " + factor.id;
	}

	private String getProcessQuery(FlowPropertyFactor factor) {
		return "SELECT DISTINCT f_owner FROM tbl_exchanges" +
			" WHERE f_flow = " + flow.id +
			" AND f_flow_property_factor = " + factor.id;
	}

}
