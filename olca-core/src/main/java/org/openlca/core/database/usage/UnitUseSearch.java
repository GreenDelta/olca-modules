package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

/**
 * Searches for the usage of flow property factors of a given flow in other
 * entities of the database.
 */
public class UnitUseSearch {

	private IDatabase database;

	public UnitUseSearch(IDatabase database) {
		this.database = database;
	}

	public List<CategorizedDescriptor> findUses(Unit unit) {
		if (unit == null || database == null)
			return Collections.emptyList();
		// only exchange, impact factor and social indicator are relevant,
		// because all others can only refer to units that are used in one of
		// them
		Set<Long> ids = Collections.singleton(unit.getId());
		List<CategorizedDescriptor> results = new ArrayList<>();
		Set<Long> categoryIds = Search.on(database).queryForIds(
				"f_impact_category", "tbl_impact_factors", ids, "f_unit");
		results.addAll(Search.on(database).queryFor(ModelType.IMPACT_METHOD,
				"f_impact_method", "tbl_impact_categories", categoryIds, "id"));
		results.addAll(Search.on(database).queryFor(ModelType.PROCESS,
				"f_owner", "tbl_exchanges", ids, "f_unit"));
		results.addAll(Search.on(database).queryFor(ModelType.SOCIAL_INDICATOR,
				ids, "f_activity_unit"));
		return results;
	}

}
