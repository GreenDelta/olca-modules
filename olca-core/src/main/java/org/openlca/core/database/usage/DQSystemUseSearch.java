package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.DQSystemDescriptor;

/**
 * Searches for the use of data quality systems in other entities. DQSystems can
 * be used in processes.
 */
public record DQSystemUseSearch(IDatabase db) implements IUseSearch {

	@Override
	public List<? extends RootDescriptor> find(TLongSet ids) {
		var q = "select id, "
		return null;
	}

	@Override
	public List<RootDescriptor> findUses(Set<Long> ids) {
		List<RootDescriptor> results = new ArrayList<>();
		results.addAll(queryFor(ModelType.PROCESS, ids, "f_dq_system", "f_exchange_dq_system", "f_social_dq_system"));
		return results;
	}

}
