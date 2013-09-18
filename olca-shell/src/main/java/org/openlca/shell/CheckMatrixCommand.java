package org.openlca.shell;

import org.jblas.DoubleMatrix;
import org.jblas.Solve;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.BlasMatrix;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryBuilder;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.matrix.ProductIndexBuilder;
import org.openlca.core.model.AllocationMethod;
import org.openlca.jblas.Library;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckMatrixCommand {

	private Logger log = LoggerFactory.getLogger(getClass());

	public void exec(Shell shell, String[] args) {
		if (!Library.isLoaded()) {
			log.error("BLAS library is not loaded");
			return;
		}
		if (args.length < 1) {
			log.error("process product expected as argument");
			return;
		}
		IDatabase database = shell.getDatabase();
		if (database == null) {
			log.error("no database connection");
			return;
		}
		LongPair processProduct = fetchProduct(args);
		if (processProduct == null) {
			log.error("the process product has no valid format; "
					+ "it must be <numbler>#<number>");
			return;
		}
		run(processProduct, database);
	}

	private LongPair fetchProduct(String[] args) {
		String productString = args[0];
		if (productString == null || productString.isEmpty())
			return null;
		try {
			String[] parts = productString.split("#");
			return new LongPair(Long.parseLong(parts[0]),
					Long.parseLong(parts[1]));
		} catch (Exception e) {
			return null;
		}
	}

	private void run(LongPair refProduct, IDatabase database) {
		try {

			log.info("check product system {}#{}", refProduct.getFirst(),
					refProduct.getSecond());

			ProductIndexBuilder builder = new ProductIndexBuilder(database);
			ProductIndex index = builder.build(refProduct, 1d);

			log.trace("Build inventory");
			InventoryBuilder matrixBuilder = new InventoryBuilder(database);
			Inventory inventory = matrixBuilder.build(index,
					AllocationMethod.USE_DEFAULT);

			BlasMatrix techMatrix = (BlasMatrix) inventory
					.getTechnologyMatrix().createRealMatrix();
			DoubleMatrix matrix = techMatrix.getNativeMatrix();

			for (int i = 1; i < index.size(); i++) {
				log.info("include product: " + index.getProductAt(i));
				int size = i + 1;
				log.info("we have a " + size + "x" + size + " matrix");
				log.info("try solve...");

				int[] range = range(size);
				DoubleMatrix subMatrix = matrix.get(range, range);
				DoubleMatrix demand = new DoubleMatrix(size, 1);
				demand.put(0, 0, 1);

				try {
					Solve.solve(subMatrix, demand);
					System.out.println(" success!");
					System.out.println();
				} catch (Throwable e) {
					printError(size, subMatrix, e);
					break;
				}

			}

		} catch (Exception e) {
			log.error("matrix check failed", e);
		}

	}

	private void printError(int size, DoubleMatrix subMatrix, Throwable e) {
		System.err.println("FAILED");
		if (size < 400) {
			System.out.println("\n\n\n\n Matlab format");
			System.out.println("A= " + subMatrix);
			System.out.println("\n\n\n Excel format");
			for (int row = 0; row < size; row++) {
				for (int col = 0; col < size; col++) {
					System.out.print(subMatrix.get(row, col) + "\t");
				}
				System.out.println();
			}
		}
		e.printStackTrace();
	}

	private int[] range(int size) {
		int[] range = new int[size];
		for (int i = 0; i < size; i++) {
			range[i] = i;
		}
		return range;
	}

}
