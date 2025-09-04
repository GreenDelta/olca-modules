package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.ProviderType;
import org.openlca.core.model.descriptors.RootDescriptor;

import gnu.trove.set.TLongSet;

public record ResultUsageSearch(IDatabase db) implements UsageSearch {

	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		if (ids.isEmpty())
			return Collections.emptySet();
		var suffix = Search.eqIn(ids);
		return QueryPlan.of(db)
				.submit(Epd.class,
						"select f_epd from tbl_epd_modules where f_result " + suffix)
				.submit(Process.class,
						"select f_owner from tbl_exchanges where default_provider_type = "
								+ ProviderType.RESULT + " and f_default_provider " + suffix)
				.submit(ProductSystem.class,
						"select f_product_system from tbl_product_system_processes " +
								"where f_process " + suffix)
				.exec();
	}

}
