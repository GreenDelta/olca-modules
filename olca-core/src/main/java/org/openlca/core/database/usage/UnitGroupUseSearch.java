package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.descriptors.RootDescriptor;

public record UnitGroupUseSearch(IDatabase db) implements UsageSearch {

	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		if (ids.isEmpty())
			return Collections.emptySet();
		return Query.of(db, FlowProperty.class,
				"select id from tbl_flow_properties " +
					"where f_unit_group " + Search.eqIn(ids))
			.call();
	}

}
