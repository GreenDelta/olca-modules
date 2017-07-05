package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

/**
 * Searches for the use of process exchanges in other entities. Exchanges of a
 * process can be used in product systems as quantitative reference or in
 * process links.
 */
public class ExchangeUseSearch {

	private IDatabase database;
	private Process process;

	public ExchangeUseSearch(IDatabase database, Process process) {
		this.database = database;
		this.process = process;
	}

	public List<CategorizedDescriptor> findUses(Exchange exchange) {
		if (exchange == null)
			return Collections.emptyList();
		return findUses(Arrays.asList(exchange));
	}

	public List<CategorizedDescriptor> findUses(List<Exchange> exchanges) {
		if (exchanges == null || exchanges.isEmpty())
			return Collections.emptyList();
		Set<Long> ids = new HashSet<>();
		Set<Long> flowIds = new HashSet<>();
		for (Exchange exchange : exchanges) {
			ids.add(exchange.getId());
			flowIds.add(exchange.flow.getId());
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
		String query = "SELECT DISTINCT f_product_system FROM tbl_process_links "
				+ "WHERE (f_provider = " + process.getId()
				+ " OR f_process = " + process.getId() + ")"
				+ "AND f_flow IN " + Search.asSqlList(flowIds);
		return query;
	}
}
