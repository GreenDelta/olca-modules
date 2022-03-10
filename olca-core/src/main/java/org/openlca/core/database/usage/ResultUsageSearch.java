package org.openlca.core.database.usage;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Epd;
import org.openlca.core.model.descriptors.RootDescriptor;

import java.util.Collections;
import java.util.Set;

public record ResultUsageSearch(IDatabase db) implements UsageSearch {

	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		if (ids.isEmpty())
			return Collections.emptySet();
		return Query.of(db, Epd.class,
			"select f_epd from tbl_epd_modules where f_result " + Search.eqIn(ids))
			.call();
	}

}
