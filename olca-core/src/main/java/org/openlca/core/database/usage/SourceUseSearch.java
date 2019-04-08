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
		Set<Long> docsWithSources = new HashSet<>();
		Set<Long> methodsWithSources = new HashSet<>();
		docsWithSources.addAll(queryForIds("id", "tbl_process_docs", ids, "f_publication"));
		Set<Long> docIds = getIds("tbl_process_docs");
		Set<Long> methodIds = getIds("tbl_impact_methods");
		Set<Long> sourceOwnerIds = queryForIds("f_owner", "tbl_source_links", ids, "f_source");
		for (long id : sourceOwnerIds) {
			if (docIds.contains(id)) {
				docsWithSources.add(id);
			} else if (methodIds.contains(id)) {
				methodsWithSources.add(id);
			}
		}
		Set<CategorizedDescriptor> result = new HashSet<>();
		result.addAll(queryFor(ModelType.PROCESS, docsWithSources, "f_process_doc"));
		result.addAll(queryFor(ModelType.DQ_SYSTEM, ids, "f_source"));
		result.addAll(loadDescriptors(ModelType.IMPACT_METHOD, methodsWithSources));
		return new ArrayList<>(result);
	}
}
