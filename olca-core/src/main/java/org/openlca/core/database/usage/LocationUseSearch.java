package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;

/**
 * Searches for the use of locations in other entities. Locations can be used in
 * flows and processes.
 */
public class LocationUseSearch extends BaseUseSearch<LocationDescriptor> {

	public LocationUseSearch(IDatabase database) {
		super(database);
	}

	@Override
	public List<CategorizedDescriptor> findUses(Set<Long> ids) {
		List<CategorizedDescriptor> results = new ArrayList<>();
		results.addAll(queryFor(ModelType.FLOW, ids, "f_location"));
		results.addAll(queryFor(ModelType.PROCESS, ids, "f_location"));
		return results;
	}

}
