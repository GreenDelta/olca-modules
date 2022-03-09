package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.RootDescriptor;

public record FlowPropertyUseSearch(IDatabase db) implements UsageSearch {

	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		if (ids.isEmpty())
			return Collections.emptySet();
		var suffix = Search.eqIn(ids);
		return QueryPlan.of(db)
			.submit(Flow.class,
				"select f_flow from tbl_flow_property_factors " +
					"where f_flow_property " + suffix)
			.submit(UnitGroup.class,
				"select id from tbl_unit_groups " +
					"where f_default_flow_property " + suffix)
			.submit(SocialIndicator.class,
				"select id from tbl_social_indicators " +
					"where f_activity_quantity " + suffix)
			.exec();
	}
}
