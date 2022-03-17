package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.RootDescriptor;

/**
 * Searches for the use of process exchanges in other entities. Exchanges of a
 * process can be used in product systems as quantitative reference or in
 * process links.
 */
public record ExchangeUseSearch(IDatabase database, Process process) {

	public List<RootDescriptor> findUses(Exchange exchange) {
		if (exchange == null)
			return Collections.emptyList();
		return findUses(List.of(exchange));
	}

	public List<RootDescriptor> findUses(List<Exchange> exchanges) {
		if (exchanges == null || exchanges.isEmpty())
			return Collections.emptyList();
		Set<Long> ids = new HashSet<>();
		Set<Long> flowIds = new HashSet<>();
		for (Exchange exchange : exchanges) {
			ids.add(exchange.id);
			flowIds.add(exchange.flow.id);
		}
		Set<Long> systemIds = new HashSet<>();
		systemIds.addAll(Search.on(database).queryForIds(
			getProductSystemQuery(flowIds)));
		systemIds.addAll(Search.on(database).queryForIds(
			ModelType.PRODUCT_SYSTEM, ids, "f_reference_exchange"));
		return new ArrayList<>(
			new ProductSystemDao(database).getDescriptors(systemIds));
	}

	private String getProductSystemQuery(Set<Long> flowIds) {
		return "SELECT DISTINCT f_product_system FROM tbl_process_links "
			+ "WHERE (f_provider = " + process.id
			+ " OR f_process = " + process.id + ")"
			+ "AND f_flow IN " + Search.asSqlList(flowIds);
	}
}
