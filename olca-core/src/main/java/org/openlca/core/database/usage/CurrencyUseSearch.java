package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.RootDescriptor;

/**
 * Searches for the use of currencies in other entities.
 */
public record CurrencyUseSearch(IDatabase db) implements UsageSearch {

	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		if (ids.isEmpty())
			return Collections.emptySet();
		var suffix = Search.eqIn(ids);
		return QueryPlan.of(db)
			.submit(Process.class,
				"select f_owner from tbl_exchanges where f_currency " + suffix)
			.submit(Currency.class,
				"select id from tbl_currencies where f_reference_currency " + suffix)
			.exec();
	}
}
