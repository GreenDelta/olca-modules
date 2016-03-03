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
 * in processes and other currencies.
 */
public class CurrencyUseSearch extends BaseUseSearch<CurrencyDescriptor> {

	public CurrencyUseSearch(IDatabase database) {
		super(database);
	}

	@Override
	public List<CategorizedDescriptor> findUses(Set<Long> ids) {
		List<CategorizedDescriptor> results = new ArrayList<>();
		Set<Long> processIds = queryForIds("f_owner", "tbl_exchanges", ids,
				"f_currency");
		results.addAll(queryFor(ModelType.PROCESS, processIds, "id"));
		results.addAll(queryFor(ModelType.PROCESS, ids, "f_currency"));
		results.addAll(queryFor(ModelType.CURRENCY, ids, "f_reference_currency"));
		return results;
	}

}
