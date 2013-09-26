package org.openlca.core.matrix;

import java.sql.Connection;

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
	public void autoComplete(ProductSystem system) {
		if (system == null || system.getReferenceExchange() == null
				|| system.getReferenceProcess() == null)
			return;
		Process refProcess = system.getReferenceProcess();
		Flow refProduct = system.getReferenceExchange().getFlow();
		if (refProduct == null)
			return;
		LongPair ref = new LongPair(refProcess.getId(), refProduct.getId());
		autoComplete(system, ref);
	}

	@Override
	public void autoComplete(ProductSystem system, LongPair processProduct) {
		try (Connection con = database.createConnection()) {
			log.trace("auto complete product system {}", system);
			run(system, processProduct);
			log.trace("update product system in database");
			database.createDao(ProductSystem.class).update(system);
			database.getEntityFactory().getCache().evict(ProductSystem.class);
		} catch (Exception e) {
			log.error("Failed to auto complete product system " + system, e);
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
		ProcessLinkIndex oldLinks = new ProcessLinkIndex();
		ProcessLinkIndex newLinks = new ProcessLinkIndex();
		addSystemLinks(system, oldLinks);
		for (LongPair input : index.getLinkedInputs()) {
			LongPair output = index.getLinkedOutput(input);
			if (output == null)
				continue;
			long provider = output.getFirst();
			long recipient = input.getFirst();
			long flow = input.getSecond();
			if (!system.getProcesses().contains(provider))
				system.getProcesses().add(provider);
			if (oldLinks.contains(provider, recipient, flow))
				continue;
			if (newLinks.contains(provider, recipient, flow))
				continue;
			newLinks.put(provider, recipient, flow);
		}
		system.getProcessLinks().addAll(newLinks.createLinks());
	}

	private void addSystemLinks(ProductSystem system, ProcessLinkIndex linkIndex) {
		for (ProcessLink link : system.getProcessLinks()) {
			linkIndex.put(link);
		}
	}

}
