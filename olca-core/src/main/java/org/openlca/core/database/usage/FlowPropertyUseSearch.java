package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;

/**
 * Searches for the use of flow properties in other entities. Flow properties
 * can be used in flows (in flow property factors) and unit groups (as default
 * flow property).
 */
public class FlowPropertyUseSearch extends
		BaseUseSearch<FlowPropertyDescriptor> {

	public FlowPropertyUseSearch(IDatabase database) {
		super(database);
	}

	@Override
	public List<CategorizedDescriptor> findUses(Set<Long> ids) {
		List<CategorizedDescriptor> results = new ArrayList<>();
		results.addAll(queryFor(ModelType.FLOW, "f_flow",
				"tbl_flow_property_factors", ids, "f_flow_property"));
		results.addAll(queryFor(ModelType.UNIT_GROUP, ids,
				"f_default_flow_property"));
		return results;
	}

}
