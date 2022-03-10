package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Project;
import org.openlca.core.model.Result;
import org.openlca.core.model.descriptors.RootDescriptor;

public record ImpactMethodUseSearch(IDatabase db) implements UsageSearch {

	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		if (ids.isEmpty())
			return Collections.emptySet();
		var suffix = Search.eqIn(ids);
		return QueryPlan.of(db)
			.submit(Project.class,
				"select id from tbl_projects where f_impact_method " + suffix)
			.submit(Result.class,
				"select id from tbl_results where f_impact_method " + suffix)
			.exec();
	}
}
