package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.RootDescriptor;

/**
 * Searches for the use of data quality systems in other entities. DQSystems can
 * be used in processes.
 */
public record DQSystemUseSearch(IDatabase db) implements UsageSearch {

	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		if (ids.isEmpty())
			return Collections.emptySet();
		var q = "select id, f_dq_system, f_exchange_dq_system," +
			" f_social_dq_system from tbl_processes";
		var processIds = new HashSet<Long>();
		NativeSql.on(db).query(q, r -> {
			for (int i = 2; i < 5; i++) {
				if (ids.contains(r.getLong(i))) {
					processIds.add(r.getLong(1));
					break;
				}
			}
			return true;
		});
		return processIds.isEmpty()
		? Collections.emptySet()
		: new HashSet<>(db.getDescriptors(Process.class, processIds));
	}

}
