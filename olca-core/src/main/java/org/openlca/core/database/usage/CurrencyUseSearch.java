package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.CurrencyDescriptor;

/**
 * Searches for the use of currencies in other entities.
 */
public record CurrencyUseSearch(IDatabase db) implements IUseSearch {

	@Override
	public List<? extends RootDescriptor> find(TLongSet ids) {

		var processQuery

		return null;
	}

	@Override
	public List<RootDescriptor> findUses(Set<Long> ids) {
		var results = new ArrayList<RootDescriptor>();
		var processIds = queryForIds("f_owner", "tbl_exchanges", ids, "f_currency");
		results.addAll(queryFor(ModelType.PROCESS, processIds, "id"));
		results.addAll(queryFor(ModelType.CURRENCY, ids, "f_reference_currency"));
		return results;
	}

}
