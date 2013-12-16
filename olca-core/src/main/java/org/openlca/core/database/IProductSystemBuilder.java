package org.openlca.core.database;

import org.openlca.core.jobs.IProgressMonitor;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductSystemBuilder;
import org.openlca.core.matrix.cache.MatrixCache;
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
	ProductSystem autoComplete(ProductSystem system);

	/**
	 * Creates the supply chain of the given process-product in the product
	 * system.
	 */
	ProductSystem autoComplete(ProductSystem system, LongPair processProduct);

	class Factory {

		private static Logger log = LoggerFactory.getLogger(Factory.class);

		private Factory() {
		}

		public static IProductSystemBuilder create(MatrixCache matrixCache,
				IProgressMonitor monitor, boolean preferSystemProcesses) {
			log.trace("Create normal product system builder");
			ProductSystemBuilder builder = new ProductSystemBuilder(
					matrixCache, preferSystemProcesses);
			builder.setProgressMonitor(monitor);
			return builder;
		}

		public static IProductSystemBuilder create(MatrixCache matrixCache,
				IProgressMonitor monitor, boolean preferSystemProcesses,
				double cutoff) {
			log.trace("Create product system builder with cut-off = {}", cutoff);
			return create(matrixCache, monitor, preferSystemProcesses);
			// TODO: no cutoff!
		}
	}
}
