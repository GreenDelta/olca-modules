package org.openlca.core.matrix;

import java.sql.PreparedStatement;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.product.index.ITechIndexBuilder;
import org.openlca.core.matrix.product.index.TechIndexBuilder;
import org.openlca.core.matrix.product.index.TechIndexCutoffBuilder;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.impl.Constants;
import gnu.trove.set.hash.TLongHashSet;

/**
 * Builds or auto-completes a product system according to a given configuration.
 */
public class ProductSystemBuilder {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private final MatrixCache matrixCache;
	private final IDatabase database;
	private final LinkingConfig config;

	/**
	 * Create a new product system builder.
	 */
	public ProductSystemBuilder(MatrixCache matrixCache, LinkingConfig config) {
		this.matrixCache = matrixCache;
		this.database = matrixCache.getDatabase();
		this.config = config;
	}

	/**
	 * Creates a new product system for the given process and runs the
	 * auto-complete functions with the linking configuration of this build. The
	 * returned system is not saved to the database.
	 */
	public ProductSystem build(Process process) {
		if (process == null)
			return null;
		ProductSystem system = ProductSystem.from(process);
		autoComplete(system);
		return system;
	}

	/**
	 * Auto-completes the given product system starting with the reference
	 * process of the system and following all product inputs and waste outputs
	 * recursively to link them to a provider process. After this function the
	 * product system will contain an updated set of process IDs and process
	 * links. The meta-data of the product system are not changed. When you then
	 * want to save these updated process IDs and process links in the database
	 * you can call the {@link #saveLinks(ProductSystem)} function.
	 */
	public void autoComplete(ProductSystem system) {
		if (system == null
				|| system.referenceExchange == null
				|| system.referenceProcess == null)
			return;
		Process refProcess = system.referenceProcess;
		Flow refProduct = system.referenceExchange.flow;
		if (refProduct == null)
			return;
		ProcessProduct ref = ProcessProduct.of(refProcess, refProduct);
		autoComplete(system, ref);
	}

	/**
	 * Does the same as {@link #autoComplete(ProductSystem)} but starts the
	 * linking at the given process product which can be arbitrary product in
	 * the supply chain of the given system.
	 */
	public void autoComplete(ProductSystem system, ProcessProduct product) {
		log.trace("auto complete product system {}", system);
		log.trace("build product index");
		ITechIndexBuilder builder;
		if (config.cutoff == null || config.cutoff == 0) {
			builder = new TechIndexBuilder(matrixCache, system, config);
		} else {
			builder = new TechIndexCutoffBuilder(
					matrixCache, system, config);
		}
		TechIndex index = builder.build(product);
		log.trace("create new process links");
		addLinksAndProcesses(system, index);
	}

	private void addLinksAndProcesses(ProductSystem system, TechIndex index) {
		TLongHashSet linkIds = new TLongHashSet(Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR, -1);
		for (ProcessLink link : system.processLinks) {
			linkIds.add(link.exchangeId);
		}
		for (LongPair exchange : index.getLinkedExchanges()) {
			ProcessProduct provider = index.getLinkedProvider(exchange);
			if (provider == null)
				continue;
			system.processes.add(provider.id());
			system.processes.add(exchange.first);
			long exchangeId = exchange.second;
			if (linkIds.add(exchangeId)) {
				ProcessLink link = new ProcessLink();
				link.exchangeId = exchangeId;
				link.flowId = provider.flowId();
				link.processId = exchange.first;
				link.providerId = provider.id();
				system.processLinks.add(link);
			}
		}
	}

	/**
	 * Saves the updated process links and IDs of the given product system in
	 * the databases. Note that if the product system is already contained in
	 * the database (i.e. has an ID > 0) this function will not update the other
	 * meta-data of the system as it is intended to call this function after an
	 * {@link #autoComplete(ProductSystem)} call in this case.
	 */
	public ProductSystem saveUpdates(ProductSystem system) {
		if (system == null)
			return null;
		try {
			ProductSystemDao dao = new ProductSystemDao(database);
			if (system.id == 0L) {
				log.trace("ID == 0 -> save as new product system");
				return dao.insert(system);
			}
			log.trace("update product system tables");
			cleanTables(system.id);
			insertLinks(system);
			insertProcesses(system);
			log.trace("reload system");
			database.getEntityFactory().getCache().evict(ProductSystem.class);
			return dao.getForId(system.id);
		} catch (Exception e) {
			log.error("failed to update database", e);
			return null;
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

	private void insertLinks(ProductSystem system) throws Exception {
		List<ProcessLink> links = system.processLinks;
		log.trace("insert {} process links", links.size());
		String stmt = "insert into tbl_process_links(f_product_system, "
				+ "f_provider, f_process, f_flow, f_exchange) "
				+ "values (?, ?, ?, ?, ?)";
		NativeSql.on(database).batchInsert(stmt, links.size(),
				(int i, PreparedStatement ps) -> {
					ProcessLink link = links.get(i);
					ps.setLong(1, system.id);
					ps.setLong(2, link.providerId);
					ps.setLong(3, link.processId);
					ps.setLong(4, link.flowId);
					ps.setLong(5, link.exchangeId);
					return true;
				});
	}

	private void insertProcesses(ProductSystem system) throws Exception {
		long[] ids = system.processes.stream()
				.mapToLong(Long::longValue).toArray();
		log.trace("insert {} system processes", ids.length);
		String stmt = "insert into tbl_product_system_processes("
				+ "f_product_system, f_process) values (?, ?)";
		NativeSql.on(database).batchInsert(stmt, ids.length,
				(int i, PreparedStatement ps) -> {
					ps.setLong(1, system.id);
					ps.setLong(2, ids[i]);
					return true;
				});
	}
}
