package org.openlca.shell;

import java.util.ArrayList;
import java.util.List;

import org.jblas.DoubleMatrix;
import org.jblas.Solve;
import org.openlca.core.database.IDatabase;
import org.openlca.core.indices.ExchangeTable;
import org.openlca.core.indices.FlowIndex;
import org.openlca.core.indices.LongPair;
import org.openlca.core.indices.ProductIndex;
import org.openlca.core.indices.ProductIndexBuilder;
import org.openlca.core.math.BlasMatrix;
import org.openlca.core.matrices.InventoryMatrix;
import org.openlca.core.matrices.InventoryMatrixBuilder;
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

			ProductIndexBuilder builder = new ProductIndexBuilder(database,
					refProduct);
			ProductIndex index = builder.build();
			List<Long> processIds = new ArrayList<>();
			for (int i = 0; i < index.size(); i++) {
				LongPair product = index.getProductAt(i);
				Long processId = product.getFirst();
				if (!processIds.contains(processId))
					processIds.add(processId);
			}

			log.trace("Build exchange table");
			ExchangeTable table = new ExchangeTable(database, processIds);

			log.trace("Build flow index");
			FlowIndex flowIndex = new FlowIndex(index, table);

			log.trace("Build inventory matrix");
			InventoryMatrixBuilder matrixBuilder = new InventoryMatrixBuilder(
					index, flowIndex, table);
			InventoryMatrix inventory = matrixBuilder.build();

			BlasMatrix techMatrix = (BlasMatrix) inventory
					.getTechnologyMatrix();
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
		if (size < 100) {
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
