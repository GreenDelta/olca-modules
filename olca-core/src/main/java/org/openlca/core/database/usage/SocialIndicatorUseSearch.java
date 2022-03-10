package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.RootDescriptor;

public record SocialIndicatorUseSearch(IDatabase db) implements UsageSearch {

	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		if (ids.isEmpty())
			return Collections.emptySet();
		return Query.of(db, Process.class,
			"select f_process from tbl_social_aspects where f_indicator "
				+ Search.eqIn(ids))
			.call();
	}
}
