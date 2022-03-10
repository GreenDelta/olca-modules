package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.RootDescriptor;

/**
 * Searches for the use of actors in other entities.
 */
public record ActorUseSearch(IDatabase db) implements UsageSearch {

	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		if (ids.isEmpty())
			return Collections.emptySet();
		try {
			var exec = Executors.newFixedThreadPool(2);
			var results = List.of(
				exec.submit(() -> findInProcessDocs(ids)),
				exec.submit(() -> findInEpds(ids)));
			exec.shutdown();
			var descriptors = new HashSet<RootDescriptor>();
			for (var r : results) {
				descriptors.addAll(r.get());
			}
			return descriptors;
		} catch (Exception e) {
			throw new RuntimeException("failed to search for actor usage", e);
		}
	}

	private List<? extends RootDescriptor> findInProcessDocs(TLongSet ids) {
		var sql = """
			select p.id,
				doc.f_reviewer,
			  doc.f_dataset_owner,
			  doc.f_data_generator,
			  doc.f_data_documentor
			from tbl_processes p inner join tbl_process_docs doc
			on p.f_process_doc = doc.id
			""";
		return collect(ids, sql, Process.class, 5);
	}

	private List<? extends RootDescriptor> findInEpds(TLongSet ids) {
		var sql = """
			select id,
				f_manufacturer,
				f_verifier,
				f_program_operator
			from tbl_epds
			""";
		return collect(ids, sql, Epd.class, 4);
	}

	private List<? extends RootDescriptor> collect(
		TLongSet ids, String query, Class<? extends RootEntity> type, int len) {
		var collected = new HashSet<Long>();
		NativeSql.on(db).query(query, r -> {
			for (int col = 2; col <= len; col++) {
				var actorId = r.getLong(col);
				if (ids.contains(actorId)) {
					collected.add(r.getLong(1));
					break;
				}
			}
			return true;
		});
		return collected.isEmpty()
			? Collections.emptyList()
			: db.getDescriptors(type, collected);
	}

}
