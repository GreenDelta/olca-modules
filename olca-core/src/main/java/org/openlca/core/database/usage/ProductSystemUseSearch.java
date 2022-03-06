package org.openlca.core.database.usage;

import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Project;
import org.openlca.core.model.Result;
import org.openlca.core.model.descriptors.RootDescriptor;

public record ProductSystemUseSearch(IDatabase db) implements IUseSearch {

	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		var suffix = Search.eqIn(ids);
		return QueryPlan.of(db)
			.submit(Result.class,
				"select id from tbl_results where f_product_system "
					+ suffix)
			.submit(Project.class,
				"select f_project from tbl_project_variants where f_product_system "
					+ suffix)
			.exec();
	}
}
