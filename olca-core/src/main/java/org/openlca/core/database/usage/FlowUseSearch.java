package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Epd;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Process;
import org.openlca.core.model.Result;
import org.openlca.core.model.descriptors.RootDescriptor;

public record FlowUseSearch(IDatabase db) implements UsageSearch {

	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		if (ids.isEmpty())
			return Collections.emptySet();
		var suffix = " where f_flow " + Search.eqIn(ids);
		return QueryPlan.of(db)
			.submit(ImpactCategory.class,
				"select f_impact_category from tbl_impact_factors" + suffix)
			.submit(Process.class,
				"select f_owner from tbl_exchanges" + suffix)
			.submit(Result.class,
				"select f_result from tbl_flow_results" + suffix)
			.submit(Epd.class,
				"select id from tbl_epds" + suffix)
			.exec();
	}
}
