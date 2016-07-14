package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.SourceDescriptor;

/**
 * Searches for the use of sources in other entities. Sources can be used in
 * processes.
 */
public class SourceUseSearch extends BaseUseSearch<SourceDescriptor> {

	public SourceUseSearch(IDatabase database) {
		super(database);
	}

	@Override
	public List<CategorizedDescriptor> findUses(Set<Long> ids) {
		Set<Long> processDocIds = new HashSet<>();
		processDocIds.addAll(queryForIds("id", "tbl_process_docs", ids, "f_publication"));
		processDocIds.addAll(queryForIds("f_process_doc", "tbl_process_sources", ids, "f_source"));
		Set<CategorizedDescriptor> result = new HashSet<>();
		result.addAll(queryFor(ModelType.PROCESS, processDocIds, "f_process_doc"));
		result.addAll(queryFor(ModelType.DQ_SYSTEM, ids, "f_source"));
		return new ArrayList<>(result);
	}
}
