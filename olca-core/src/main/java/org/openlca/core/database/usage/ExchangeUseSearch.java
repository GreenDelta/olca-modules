package org.openlca.core.database.usage;

import static org.openlca.core.database.usage.Search.asSqlList;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.RootDescriptor;

/**
 * Searches for the use of process exchanges in other entities. Exchanges of a
 * process can be used in product systems as quantitative reference or in
 * process links.
 */
public record ExchangeUseSearch(IDatabase db, Process process) {

	public List<? extends RootDescriptor> findUses(Exchange e) {
		return e != null
			? findUses(List.of(e))
			: Collections.emptyList();
	}

	public List<? extends RootDescriptor> findUses(List<Exchange> exs) {
		if (exs == null || exs.isEmpty())
			return Collections.emptyList();

		var exchangeIds = new HashSet<Long>();
		var flowIds = new HashSet<Long>();
		for (var e : exs) {
			exchangeIds.add(e.id);
			if (e.flow != null) {
				flowIds.add(e.flow.id);
			}
		}

		var systemIds = new HashSet<Long>();

		// search in links
		systemIds.addAll(
			Search.on(db).queryForIds(linkUsageOf(flowIds, exchangeIds)));

		// search in quantitative references
		systemIds.addAll(
			Search.on(db).queryForIds(
				ModelType.PRODUCT_SYSTEM, exchangeIds, "f_reference_exchange"));

		return db.getDescriptors(ProductSystem.class, systemIds);
	}

	private String linkUsageOf(Set<Long> flowIds, Set<Long> exchangeIds) {
		return "SELECT DISTINCT f_product_system FROM tbl_process_links WHERE ("
			+ "f_provider = " + process.id + " AND f_flow IN " + asSqlList(flowIds)
			+ ") OR ("
			+ "f_process = " + process.id + " AND f_exchange IN " + asSqlList(exchangeIds)
			+ ")";
	}
}
