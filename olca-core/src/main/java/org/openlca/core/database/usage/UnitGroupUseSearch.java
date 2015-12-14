package org.openlca.core.database.usage;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;

/**
 * Searches for the use of unit groups in other entities. Unit groups can be
 * used in flow properties.
 */
public class UnitGroupUseSearch extends BaseUseSearch<UnitGroupDescriptor> {

	public UnitGroupUseSearch(IDatabase database) {
		super(database);
	}

	@Override
	public List<CategorizedDescriptor> findUses(Set<Long> ids) {
		return queryFor(ModelType.FLOW_PROPERTY, ids, "f_unit_group");
	}

}
