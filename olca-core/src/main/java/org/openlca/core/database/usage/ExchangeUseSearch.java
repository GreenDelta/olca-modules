package org.openlca.core.database.usage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches for the use of process exchanges in other entities. Exchanges of a
 * process can be used in product systems as quantitative reference or in
 * process links.
 */
public class ExchangeUseSearch implements IUseSearch<Exchange> {

	private IDatabase database;
	private Process process;

	public ExchangeUseSearch(IDatabase database, Process process) {
		this.database = database;
		this.process = process;
	}

	@Override
	public List<BaseDescriptor> findUses(Exchange exchange) {
		if (exchange == null)
			return Collections.emptyList();
		return findUses(Arrays.asList(exchange));
	}

	public List<BaseDescriptor> findUses(List<Exchange> exchanges) {
		if (exchanges == null || exchanges.isEmpty())
			return Collections.emptyList();
		try {
			Set<Long> refIds = searchInReferences(exchanges);
			Set<Long> linkIds = searchInLinks(exchanges);
			Set<Long> all = new TreeSet<>();
			all.addAll(refIds);
			all.addAll(linkIds);
			return loadDescriptors(all);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to search for exchange usages", e);
			return Collections.emptyList();
		}
	}

	private List<BaseDescriptor> loadDescriptors(Set<Long> systemIds) {
		if (systemIds == null || systemIds.isEmpty())
			return Collections.emptyList();
		ProductSystemDao dao = new ProductSystemDao(database);
		return new ArrayList<BaseDescriptor>(dao.getDescriptors(systemIds));
	}

	private Set<Long> searchInReferences(List<Exchange> exchanges)
			throws Exception {
		Set<Long> exchangeIds = new TreeSet<>();
		for (Exchange exchange : exchanges)
			exchangeIds.add(exchange.getId());
		if (exchangeIds.isEmpty())
			return Collections.emptySet();
		String query = "select id from tbl_product_systems where " +
				"f_reference_exchange in " + asSqlList(exchangeIds);
		return queryIds(query);
	}

	private Set<Long> searchInLinks(List<Exchange> exchanges) throws Exception {
		if (process == null)
			return Collections.emptySet();
		Set<Long> flowIds = new TreeSet<>();
		for (Exchange exchange : exchanges) {
			if (exchange.getFlow() != null)
				flowIds.add(exchange.getFlow().getId());
		}
		if (flowIds.isEmpty())
			return Collections.emptySet();
		long processId = process.getId();
		String query = "select distinct f_product_system from tbl_process_links "
				+ "where (f_provider = " + processId
				+ "or f_recipient = " + processId + ") "
				+ "and f_flow in " + asSqlList(flowIds);
		return queryIds(query);
	}

	private String asSqlList(Set<Long> ids) {
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		Iterator<Long> it = ids.iterator();
		while (it.hasNext()) {
			long next = it.next();
			builder.append(next);
			if (it.hasNext())
				builder.append(',');
		}
		builder.append(')');
		return builder.toString();
	}

	private Set<Long> queryIds(String query) throws SQLException {
		final Set<Long> systemIds = new TreeSet<>();
		NativeSql.on(database).query(query, new NativeSql.QueryResultHandler() {
			@Override
			public boolean nextResult(ResultSet result) throws SQLException {
				systemIds.add(result.getLong(1));
				return true;
			}
		});
		return systemIds;
	}
}
