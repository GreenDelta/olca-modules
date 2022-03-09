package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Process;
import org.openlca.core.model.Result;
import org.openlca.core.model.descriptors.RootDescriptor;

public record LocationUseSearch(IDatabase db) implements UsageSearch {

	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		if (ids.isEmpty())
			return Collections.emptySet();
		var suffix = " where f_location " + Search.eqIn(ids);
		return QueryPlan.of(db)
			.submit(Flow.class,
				"select id from tbl_flows" + suffix)
			.submit(Process.class,
				"select id from tbl_processes" + suffix)
			.submit(Process.class,
				"select distinct f_owner from tbl_exchanges" + suffix)
			.submit(ImpactCategory.class,
				"select distinct f_impact_category from tbl_impact_factors" + suffix)
			.submit(Result.class,
				"select distinct f_result from tbl_flow_results" + suffix)
			.exec();
	}
}
