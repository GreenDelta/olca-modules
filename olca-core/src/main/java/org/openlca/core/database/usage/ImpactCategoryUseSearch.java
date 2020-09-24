package org.openlca.core.database.usage;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;

public class ImpactCategoryUseSearch extends
		BaseUseSearch<ImpactDescriptor> {

	ImpactCategoryUseSearch(IDatabase db) {
		super(db);
	}

	@Override
	public List<CategorizedDescriptor> findUses(Set<Long> impactIDs) {
		Set<Long> methodIDs = queryForIds(
				"f_impact_method", "tbl_impact_links",
				impactIDs, "f_impact_category");
		return loadDescriptors(ModelType.IMPACT_METHOD, methodIDs);
	}

}
