package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.RootDescriptor;

public record ProcessUseSearch(IDatabase db) implements UsageSearch {

	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		if (ids.isEmpty())
			return Collections.emptySet();
		var suffix = Search.eqIn(ids);
		return QueryPlan.of(db)
			.submit(ProductSystem.class,
				"select f_product_system from tbl_product_system_processes " +
					"where f_process " + suffix)
			.submit(Process.class,
				"select f_owner from tbl_exchanges " +
					"where f_default_provider " + suffix)
			.exec();
	}
}
