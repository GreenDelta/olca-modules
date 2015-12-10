package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;

/**
 * Searches for the use of flows in other entities. Flows can be used in
 * processes and impact methods.
 */
public class FlowUseSearch extends BaseUseSearch<FlowDescriptor> {

	public FlowUseSearch(IDatabase database) {
		super(database);
	}

	@Override
	public List<CategorizedDescriptor> findUses(Set<Long> ids) {
		List<CategorizedDescriptor> results = new ArrayList<>();
		Set<Long> categoryIds = queryForIds("f_impact_category",
				"tbl_impact_factors", ids, "f_flow");
		results.addAll(queryFor(ModelType.IMPACT_METHOD, "f_impact_method",
				"tbl_impact_categories", categoryIds, "id"));
		results.addAll(queryFor(ModelType.PROCESS, "f_owner", "tbl_exchanges",
				ids, "f_flow"));
		return results;
	}

}
