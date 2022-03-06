package org.openlca.core.database.usage;

import java.util.List;
import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;

/**
 * Searches for the use of unit groups in other entities. Unit groups can be
 * used in flow properties.
 */
public record UnitGroupUseSearch(IDatabase db) implements IUseSearch {

	@Override
	public List<? extends RootDescriptor> find(TLongSet ids) {
		var q = "select id from tbl_flow_properties " +
			"where f_unit_group " + Search.eqIn(ids);
		return Search.collect(db, q, FlowProperty.class);
	}

}
