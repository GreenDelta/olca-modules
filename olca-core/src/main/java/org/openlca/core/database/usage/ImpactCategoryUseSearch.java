package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Result;
import org.openlca.core.model.descriptors.RootDescriptor;

public record ImpactCategoryUseSearch(IDatabase db) implements UsageSearch {

	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		if (ids.isEmpty())
			return Collections.emptySet();
		var suffix = Search.eqIn(ids);
		return QueryPlan.of(db)
			.submit(ImpactMethod.class,
				"select f_impact_method from tbl_impact_links " +
					"where f_impact_category " + suffix)
			.submit(Result.class,
				"select f_result from tbl_impact_results " +
					"where f_impact_category " + suffix)
			.exec();
	}

}
