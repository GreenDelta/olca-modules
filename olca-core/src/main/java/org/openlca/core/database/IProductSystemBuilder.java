package org.openlca.core.database;

import org.openlca.core.database.internal.ProductSystemBuilder;
import org.openlca.core.database.internal.ProductSystemCutoffBuilder;
import org.openlca.core.jobs.IProgressMonitor;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A product system builder creates the database model of a product system. The
 * processes and links between these processes are directly inserted into the
 * respective tables.
 */
public interface IProductSystemBuilder {

	/**
	 * Creates the supply-chain of a product system starting with the reference
	 * process of the system.
	 */
	void autoComplete(ProductSystem system);

	/**
	 * Creates the supply chain of the given process in the product system.
	 */
	void autoComplete(ProductSystem system, Process process);

	class Factory {

		private static Logger log = LoggerFactory.getLogger(Factory.class);

		private Factory() {
		}

		public static IProductSystemBuilder create(IDatabase database,
				IProgressMonitor monitor, boolean preferSystemProcesses) {
			log.trace("Create normal product system builder");
			ProductSystemBuilder builder = new ProductSystemBuilder(database,
					preferSystemProcesses);
			builder.setProgressMonitor(monitor);
			return builder;
		}

		public static IProductSystemBuilder create(IDatabase database,
				IProgressMonitor monitor, boolean preferSystemProcesses,
				double cutoff) {
			log.trace("Create product system builder with cut-off = {}", cutoff);
			ProductSystemCutoffBuilder builder = new ProductSystemCutoffBuilder(
					database, cutoff);
			builder.setPreferSystemProcesses(preferSystemProcesses);
			// TODO: progress monitor
			return builder;
		}
	}
}
