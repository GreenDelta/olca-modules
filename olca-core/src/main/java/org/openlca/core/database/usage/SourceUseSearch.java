package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Epd;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.RootDescriptor;

public record SourceUseSearch(IDatabase db) implements UsageSearch {

	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		if (ids.isEmpty())
			return Collections.emptySet();
		var suffix = Search.eqIn(ids);
		return QueryPlan.of(db)
			.submit(Process.class,
				"select p.id from tbl_processes p inner join " +
					"tbl_process_docs doc on p.f_process_doc = doc.id " +
					"inner join tbl_source_links s on doc.id = s.f_owner " +
					"where s.f_source " + suffix)
			.submit(Process.class,
				"select p.id from tbl_processes p inner join " +
					"tbl_process_docs doc on p.f_process_doc = doc.id " +
					"where doc.f_publication " + suffix)
			.submit(Process.class,
				"select f_process from tbl_social_aspects " +
					"where f_source " + suffix)
			.submit(ImpactMethod.class,
				"select id from tbl_impact_methods where f_source " + suffix)
			.submit(ImpactCategory.class,
				"select id from tbl_impact_categories where f_source " + suffix)
			.submit(Epd.class,
				"select id from tbl_epds where f_pcr " + suffix)
			.submit(DQSystem.class,
				"select id from tbl_dq_systems where f_source " + suffix)
			.exec();
	}
}
