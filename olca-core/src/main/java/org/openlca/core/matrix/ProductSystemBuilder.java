package org.openlca.core.matrix;

import gnu.trove.impl.Constants;
import gnu.trove.set.hash.TLongHashSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.NativeSql.BatchInsertHandler;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.product.index.ITechIndexBuilder;
import org.openlca.core.matrix.product.index.LinkingMethod;
import org.openlca.core.matrix.product.index.TechIndexBuilder;
import org.openlca.core.matrix.product.index.TechIndexCutoffBuilder;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.ProductSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductSystemBuilder {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private MatrixCache matrixCache;
	private IDatabase database;
	private ProcessType preferredType;
	private LinkingMethod linkingMethod;
	private Double cutoff;

	public ProductSystemBuilder(MatrixCache matrixCache) {
		this.matrixCache = matrixCache;
		this.database = matrixCache.getDatabase();
	}

	public void setPreferredType(ProcessType preferredType) {
		this.preferredType = preferredType;
	}

	public void setLinkingMethod(LinkingMethod linkingMethod) {
		this.linkingMethod = linkingMethod;
	}

	public void setCutoff(Double cutoff) {
		this.cutoff = cutoff;
	}

	public ProductSystem autoComplete(ProductSystem system) {
		if (system == null || system.referenceExchange == null
				|| system.referenceProcess == null)
			return system;
		Process refProcess = system.referenceProcess;
		Flow refProduct = system.referenceExchange.flow;
		if (refProduct == null)
			return system;
		LongPair ref = new LongPair(refProcess.getId(), refProduct.getId());
		return autoComplete(system, ref);
	}

	public ProductSystem autoComplete(ProductSystem system,
			LongPair processProduct) {
		try (Connection con = database.createConnection()) {
			log.trace("auto complete product system {}", system);
			run(system, processProduct);
			log.trace("reload system");
			database.getEntityFactory().getCache().evict(ProductSystem.class);
			return new ProductSystemDao(database).getForId(system.getId());
		} catch (Exception e) {
			log.error("Failed to auto complete product system " + system, e);
			return null;
		}
	}

	private void run(ProductSystem system, LongPair processProduct) {
		log.trace("build product index");
		ITechIndexBuilder builder = getProductIndexBuilder(system);
		builder.setPreferredType(preferredType);
		builder.setLinkingMethod(linkingMethod);
		TechIndex index = builder.build(processProduct);
		log.trace("create new process links");
		addLinksAndProcesses(system, index);
	}

	private ITechIndexBuilder getProductIndexBuilder(ProductSystem system) {
		if (cutoff == null || cutoff == 0)
			return new TechIndexBuilder(matrixCache, system);
		return new TechIndexCutoffBuilder(matrixCache, system, cutoff);
	}

	private void addLinksAndProcesses(ProductSystem system, TechIndex index) {
		List<ProcessLink> links = new ArrayList<>();
		TLongHashSet linkIds = new TLongHashSet(Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR, -1);
		TLongHashSet processes = new TLongHashSet(Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR, -1);

		// links and processes from system
		for (ProcessLink link : system.processLinks) {
			if (linkIds.add(link.exchangeId)) {
				links.add(link);
			}
		}
		processes.addAll(system.processes);

		// links and processes from tech-index
		for (LongPair exchange : index.getLinkedExchanges()) {
			LongPair provider = index.getLinkedProvider(exchange);
			if (provider == null)
				continue;
			processes.add(provider.getFirst());
			processes.add(exchange.getFirst());
			long exchangeId = exchange.getSecond();
			if (linkIds.add(exchangeId)) {
				ProcessLink link = new ProcessLink();
				link.exchangeId = exchangeId;
				link.flowId = provider.getSecond();
				link.processId = exchange.getFirst();
				link.providerId = provider.getFirst();
				links.add(link);
			}
		}
		updateDatabase(system, links, processes);
	}

	private void updateDatabase(ProductSystem system, List<ProcessLink> links,
			TLongHashSet processes) {
		try {
			log.trace("update product system tables");
			cleanTables(system.getId());
			insertLinks(system.getId(), links);
			insertProcesses(system.getId(), processes);
		} catch (Exception e) {
			log.error("faile to update database in process builder", e);
		}
	}

	private void cleanTables(long systemId) throws Exception {
		log.trace("clean system tables for {}", systemId);
		String sql = "delete from tbl_process_links where "
				+ "f_product_system = " + systemId;
		NativeSql.on(database).runUpdate(sql);
		sql = "delete from tbl_product_system_processes where "
				+ "f_product_system = " + systemId;
		NativeSql.on(database).runUpdate(sql);
	}

	private void insertLinks(long systemId, List<ProcessLink> links)
			throws Exception {
		log.trace("insert {} process links", links.size());
		String stmt = "insert into tbl_process_links(f_product_system, "
				+ "f_provider, f_process, f_flow, f_exchange) "
				+ "values (?, ?, ?, ?, ?)";
		NativeSql.on(database).batchInsert(stmt, links.size(),
				(int i, PreparedStatement ps) -> {
					ProcessLink link = links.get(i);
					ps.setLong(1, systemId);
					ps.setLong(2, link.providerId);
					ps.setLong(3, link.processId);
					ps.setLong(4, link.flowId);
					ps.setLong(5, link.exchangeId);
					return true;
				});
	}

	private void insertProcesses(final long systemId, TLongHashSet processes)
			throws Exception {
		log.trace("insert {} system processes", processes.size());
		final long[] processIds = processes.toArray();
		String stmt = "insert into tbl_product_system_processes("
				+ "f_product_system, f_process) values (?, ?)";
		NativeSql.on(database).batchInsert(stmt, processIds.length,
				new BatchInsertHandler() {
					@Override
					public boolean addBatch(int i, PreparedStatement ps)
							throws SQLException {
						ps.setLong(1, systemId);
						ps.setLong(2, processIds[i]);
						return true;
					}
				});
	}
}
