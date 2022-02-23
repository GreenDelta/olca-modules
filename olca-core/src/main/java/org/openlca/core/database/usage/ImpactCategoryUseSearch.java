package org.openlca.core.database.usage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;

public class ImpactCategoryUseSearch extends BaseUseSearch<ImpactDescriptor> {

	ImpactCategoryUseSearch(IDatabase db) {
		super(db);
	}

	@Override
	public List<RootDescriptor> findUses(Set<Long> impactIDs) {
		var query = "select f_impact_method, " +
				"f_impact_category from tbl_impact_links";
		var methodIDs = new HashSet<Long>();
		NativeSql.on(db).query(query, r -> {
			var impact = r.getLong(2);
			if (impactIDs.contains(impact)) {
				methodIDs.add(r.getLong(1));
			}
			return true;
		});
		return loadDescriptors(ModelType.IMPACT_METHOD, methodIDs);
	}
}
