package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.ActorDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;

/**
 * Searches for the use of actors in other entities. Actors can be used in
 * processes and projects.
 */
public class ActorUseSearch implements IUseSearch<ActorDescriptor> {

	public ActorUseSearch(IDatabase database) {
		super(database);
	}

	@Override
	public List<RootDescriptor> findUses(Set<Long> ids) {
		List<RootDescriptor> results = new ArrayList<>();
		Set<Long> processDocIds = queryForIds("id", "tbl_process_docs", ids,
				"f_reviewer", "f_dataset_owner", "f_data_generator", "f_data_documentor");
		results.addAll(queryFor(ModelType.PROCESS, processDocIds, "f_process_doc"));
		return results;
	}

}
