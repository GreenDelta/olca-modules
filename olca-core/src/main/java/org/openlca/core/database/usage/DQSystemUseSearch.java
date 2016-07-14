package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.DQSystemDescriptor;

/**
 * Searches for the use of data quality systems in other entities. DQSystems can
 * be used in processes.
 */
public class DQSystemUseSearch extends BaseUseSearch<DQSystemDescriptor> {

	public DQSystemUseSearch(IDatabase database) {
		super(database);
	}

	@Override
	public List<CategorizedDescriptor> findUses(Set<Long> ids) {
		List<CategorizedDescriptor> results = new ArrayList<>();
		results.addAll(queryFor(ModelType.PROCESS, ids, "f_dq_system", "f_exchange_dq_system", "f_social_dq_system"));
		return results;
	}

}
