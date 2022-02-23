package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.RootDescriptor;

/**
 * Searches for the usage of flow property factors of a given flow in other
 * entities of the database.
 */
public class UnitUseSearch {

	private IDatabase database;

	public UnitUseSearch(IDatabase database) {
		this.database = database;
	}

	public List<RootDescriptor> findUses(Unit unit) {
		if (unit == null || database == null)
			return Collections.emptyList();
		// only exchange, impact factor and social indicator are relevant,
		// because all others can only refer to units that are used in one of
		// them
		Set<Long> ids = Collections.singleton(unit.id);
		List<RootDescriptor> results = new ArrayList<>();
		results.addAll(Search.on(database).queryFor(ModelType.IMPACT_CATEGORY,
				"f_impact_category", "tbl_impact_factors", ids, "f_unit"));
		results.addAll(Search.on(database).queryFor(ModelType.PROCESS,
				"f_owner", "tbl_exchanges", ids, "f_unit"));
		results.addAll(Search.on(database).queryFor(ModelType.SOCIAL_INDICATOR,
				ids, "f_activity_unit"));
		return results;
	}

}
