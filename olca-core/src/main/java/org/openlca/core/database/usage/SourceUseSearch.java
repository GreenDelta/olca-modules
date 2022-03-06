package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.SourceDescriptor;

public record SourceUseSearch(IDatabase db) implements IUseSearch {

	@Override
	public List<? extends RootDescriptor> find(TLongSet ids) {
		var q = "select p.id from tbl_processes p inner join " +
			"tbl_process_docs doc on p.f_process_doc = doc.id " +
			"inner join tbl_source_links s on doc.id = s.f_owner " +
			"where s.f_source " + Search.eqIn(ids);

		return null;
	}

	@Override
	public List<RootDescriptor> findUses(Set<Long> ids) {

		Set<Long> methods = queryForIds(
			"id", "tbl_impact_methods", ids, "f_source");
		Set<Long> impacts = queryForIds(
			"id", "tbl_impact_categories", ids, "f_source");
		Set<Long> docsWithSources = new HashSet<>(
			queryForIds("id", "tbl_process_docs", ids, "f_publication"));
		Set<Long> docIds = getIds("tbl_process_docs");
		Set<Long> sourceOwnerIds = queryForIds(
			"f_owner", "tbl_source_links", ids, "f_source");
		for (long id : sourceOwnerIds) {
			if (docIds.contains(id)) {
				docsWithSources.add(id);
			}
		}
		Set<RootDescriptor> result = new HashSet<>();
		result.addAll(queryFor(ModelType.PROCESS, docsWithSources, "f_process_doc"));
		result.addAll(queryFor(ModelType.DQ_SYSTEM, ids, "f_source"));
		result.addAll(loadDescriptors(ModelType.IMPACT_METHOD, methods));
		result.addAll(loadDescriptors(ModelType.IMPACT_CATEGORY, impacts));
		return new ArrayList<>(result);
	}
}
