package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.CurrencyDescriptor;

/**
 * Searches for the use of currencies in other entities. Currencies can be used
 * in processes.
 */
public class CategoryUseSearch extends BaseUseSearch<CurrencyDescriptor> {

	public CategoryUseSearch(IDatabase database) {
		super(database);
	}

	@Override
	public List<CategorizedDescriptor> findUses(Set<Long> ids) {
		List<CategorizedDescriptor> results = new ArrayList<>();
		for (ModelType type : ModelType.values())
			if (type.isCategorized())
				results.addAll(queryFor(type, ids, "f_category"));
		return results;
	}
}
