package org.openlca.core.matrix;

import gnu.trove.impl.Constants;
import gnu.trove.set.hash.TLongHashSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.IProductSystemBuilder;
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
	private final int MAX_BATCH_SIZE = 1000;

	private IProgressMonitor progressMonitor;

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
		this.progressMonitor = progressMonitor;
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
		log.trace("create new process links");
		addLinksAndProcesses(system, index);
	}

	private void addLinksAndProcesses(ProductSystem system, ProductIndex index) {
		ProcessLinkIndex links = new ProcessLinkIndex();
		TLongHashSet processes = new TLongHashSet(Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR, -1);
		addSystemLinksAndProcesses(system, links, processes);
		for (LongPair input : index.getLinkedInputs()) {
			LongPair output = index.getLinkedOutput(input);
			if (output == null)
				continue;
			long provider = output.getFirst();
			long recipient = input.getFirst();
			long flow = input.getSecond();
			processes.add(provider);
			if (links.contains(provider, recipient, flow))
				continue;
			links.put(provider, recipient, flow);
		}
		cleanTables(system.getId());
		insertLinks(system.getId(), links.createLinks());
		insertProcesses(system.getId(), processes);
	}

	private void cleanTables(long systemId) {
		log.trace("clean system tables for {}", systemId);
		try (Connection con = database.createConnection()) {
			String sql = "delete from tbl_process_links where "
					+ "f_product_system = " + systemId;
			Statement stmt = con.createStatement();
			stmt.executeUpdate(sql);
			stmt.close();
			sql = "delete from tbl_product_system_processes where "
					+ "f_product_system = " + systemId;
			stmt = con.createStatement();
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (Exception e) {
			log.error("failed to clean system tables for " + systemId, e);
		}
	}

	private void insertLinks(long systemId, List<ProcessLink> links) {
		log.trace("insert {} process links", links.size());
		if (links.isEmpty())
			return;
		try (Connection con = database.createConnection()) {
			String stmt = "insert into tbl_process_links(f_product_system, "
					+ "f_provider, f_recipient, f_flow) values (?, ?, ?, ?)";
			PreparedStatement ps = con.prepareStatement(stmt);
			for (int i = 0; i < links.size(); i++) {
				ProcessLink link = links.get(i);
				ps.setLong(1, systemId);
				ps.setLong(2, link.getProviderId());
				ps.setLong(3, link.getRecipientId());
				ps.setLong(4, link.getFlowId());
				ps.addBatch();
				if (i % MAX_BATCH_SIZE == 0)
					ps.executeBatch();
			}
			ps.executeBatch();
			ps.close();
			log.trace("all links inserted");
		} catch (Exception e) {
			log.error("failed to insert process links", e);
		}
	}

	private void insertProcesses(long systemId, TLongHashSet processes) {
		log.trace("insert {} system processes", processes.size());
		if (processes.isEmpty())
			return;
		try (Connection con = database.createConnection()) {
			String stmt = "insert into tbl_product_system_processes("
					+ "f_product_system, f_process) values (?, ?)";
			PreparedStatement ps = con.prepareStatement(stmt);
			long[] processIds = processes.toArray();
			for (int i = 0; i < processIds.length; i++) {
				ps.setLong(1, systemId);
				ps.setLong(2, processIds[i]);
				ps.addBatch();
				if (i % MAX_BATCH_SIZE == 0)
					ps.executeBatch();
			}
			int[] rows = ps.executeBatch();
			ps.close();
			log.trace("{} inserted", rows.length);
		} catch (Exception e) {
			log.error("failed to insert system processes", e);
		}
	}

	private void addSystemLinksAndProcesses(ProductSystem system,
			ProcessLinkIndex linkIndex, TLongHashSet processes) {
		for (ProcessLink link : system.getProcessLinks()) {
			linkIndex.put(link);
		}
		for (long procId : system.getProcesses())
			processes.add(procId);
	}
}
