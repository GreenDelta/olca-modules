package org.openlca.core.matrix;

import gnu.trove.impl.Constants;
import gnu.trove.set.hash.TLongHashSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.IProductSystemBuilder;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.NativeSql.BatchInsertHandler;
import org.openlca.core.jobs.IProgressMonitor;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.ProductSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductSystemBuilder implements IProductSystemBuilder {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private MatrixCache matrixCache;
	private IDatabase database;
	private boolean preferSystemProcesses;

	public ProductSystemBuilder(MatrixCache matrixCache,
			boolean preferSystemProcesses) {
		this.matrixCache = matrixCache;
		this.database = matrixCache.getDatabase();
		this.preferSystemProcesses = preferSystemProcesses;
	}

	public void setProgressMonitor(IProgressMonitor progressMonitor) {
	}

	@Override
	public ProductSystem autoComplete(ProductSystem system) {
		if (system == null || system.getReferenceExchange() == null
				|| system.getReferenceProcess() == null)
			return system;
		Process refProcess = system.getReferenceProcess();
		Flow refProduct = system.getReferenceExchange().getFlow();
		if (refProduct == null)
			return system;
		LongPair ref = new LongPair(refProcess.getId(), refProduct.getId());
		return autoComplete(system, ref);
	}

	@Override
	public ProductSystem autoComplete(ProductSystem system,
			LongPair processProduct) {
		try (Connection con = database.createConnection()) {
			log.trace("auto complete product system {}", system);
			run(system, processProduct);
			log.trace("reload system");
			database.getEntityFactory().getCache().evict(ProductSystem.class);
			return database.createDao(ProductSystem.class).getForId(
					system.getId());
		} catch (Exception e) {
			log.error("Failed to auto complete product system " + system, e);
			return null;
		}
	}

	private void run(ProductSystem system, LongPair processProduct) {
		log.trace("build product index");
		ProductIndexBuilder builder = new ProductIndexBuilder(matrixCache);
		builder.setPreferredType(preferSystemProcesses ? ProcessType.LCI_RESULT
				: ProcessType.UNIT_PROCESS);
		ProductIndex index = builder.build(processProduct);
		log.trace(
				"built a product index with {} process products and {} links",
				index.size(), index.getLinkedInputs().size());
		log.trace("create new process links");
		addLinksAndProcesses(system, index);
	}

	private void addLinksAndProcesses(ProductSystem system, ProductIndex index) {
		ProcessLinkIndex links = new ProcessLinkIndex();
		TLongHashSet processes = new TLongHashSet(Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR, -1);
		addSystemLinksAndProcesses(system, links, processes);
		log.trace("add new processes and links");
		for (LongPair input : index.getLinkedInputs()) {
			LongPair output = index.getLinkedOutput(input);
			if (output == null)
				continue;
			long provider = output.getFirst();
			long recipient = input.getFirst();
			long flow = input.getSecond();
			processes.add(provider);
			processes.add(recipient);
			links.put(provider, recipient, flow);
		}
		updateDatabase(system, links, processes);
	}

	private void addSystemLinksAndProcesses(ProductSystem system,
			ProcessLinkIndex linkIndex, TLongHashSet processes) {
		log.trace("the system already contains {} links and {} processes",
				system.getProcessLinks().size(), system.getProcesses().size());
		for (ProcessLink link : system.getProcessLinks()) {
			linkIndex.put(link);
		}
		for (long procId : system.getProcesses())
			processes.add(procId);
	}

	private void updateDatabase(ProductSystem system, ProcessLinkIndex links,
			TLongHashSet processes) {
		try {
			log.trace("update product system tables");
			cleanTables(system.getId());
			insertLinks(system.getId(), links.createLinks());
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

	private void insertLinks(final long systemId, final List<ProcessLink> links)
			throws Exception {
		log.trace("insert {} process links", links.size());
		String stmt = "insert into tbl_process_links(f_product_system, "
				+ "f_provider, f_recipient, f_flow) values (?, ?, ?, ?)";
		NativeSql.on(database).batchInsert(stmt, links.size(),
				new BatchInsertHandler() {
					@Override
					public boolean addBatch(int i, PreparedStatement ps)
							throws SQLException {
						ProcessLink link = links.get(i);
						ps.setLong(1, systemId);
						ps.setLong(2, link.getProviderId());
						ps.setLong(3, link.getRecipientId());
						ps.setLong(4, link.getFlowId());
						return true;
					}
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
