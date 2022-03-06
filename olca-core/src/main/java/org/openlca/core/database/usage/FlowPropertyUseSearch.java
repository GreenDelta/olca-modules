package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;

public record FlowPropertyUseSearch(IDatabase db) implements IUseSearch {

	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		return null;
	}

	@Override
	public List<RootDescriptor> findUses(Set<Long> ids) {
		List<RootDescriptor> results = new ArrayList<>();
		results.addAll(queryFor(ModelType.FLOW, "f_flow",
				"tbl_flow_property_factors", ids, "f_flow_property"));
		results.addAll(queryFor(ModelType.UNIT_GROUP, ids,
				"f_default_flow_property"));
		return results;
	}

}
