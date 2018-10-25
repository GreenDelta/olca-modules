package org.openlca.core.matrix.io.olcamat;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.DataStructures;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.LinkingConfig;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.product.index.ProviderSearch;
import org.openlca.core.matrix.solvers.DenseSolver;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ProcessType;
import org.openlca.expressions.FormulaInterpreter;

/**
 * Exports a matrix into the openLCA matrix (=olcamat) format.The olcamat format
 * is still experimental so this may change in future.
 */
public class Export implements Runnable {

	private final CalculationSetup setup;
	private final IDatabase db;
	private final MatrixCache cache;
	private final IMatrixSolver solver;
	private final File dir;

	public boolean withResults = false;

	public Export(IDatabase db, File dir) {
		this(null, db, dir);
	}

	public Export(CalculationSetup setup, IDatabase db, File dir) {
		this.setup = setup;
		this.db = db;
		cache = MatrixCache.createEager(db);
		this.dir = dir;
		this.solver = new DenseSolver();
	}

	@Override
	public void run() {
		try {
			if (!dir.exists()) {
				dir.mkdirs();
			}
			InventoryMatrix imat;
			if (setup == null) {
				imat = dbInventory();
			} else {
				imat = setupInventory();
			}
			writeMatrices(imat);
			IndexWriter iw = new IndexWriter(
					imat.productIndex, imat.flowIndex);
			iw.write(db, dir);
		} catch (Exception e) {
			throw new RuntimeException("Export failed", e);
		}
	}

	private InventoryMatrix dbInventory() throws Exception {
		List<LongPair> products = cache.getProcessTable().getProviderFlows();
		TechIndex techIndex = new TechIndex(products.get(0));
		for (int i = 1; i < products.size(); i++) {
			techIndex.put(products.get(i));
		}
		dbLinks(techIndex);
		Inventory inv = Inventory.build(cache,
				techIndex, AllocationMethod.USE_DEFAULT);
		return inv.createMatrix(solver);
	}

	private InventoryMatrix setupInventory() {
		Inventory inventory = DataStructures.createInventory(setup, cache);
		ParameterTable params = DataStructures.createParameterTable(
				db, setup, inventory);
		FormulaInterpreter interpreter = params.createInterpreter();
		return inventory.createMatrix(solver, interpreter);
	}

	private void dbLinks(TechIndex idx) throws Exception {
		LinkingConfig config = new LinkingConfig();
		config.preferredType = ProcessType.UNIT_PROCESS;
		ProviderSearch search = new ProviderSearch(
				cache.getProcessTable(), config);
		Map<Long, List<CalcExchange>> exchanges = cache
				.getExchangeCache()
				.getAll(idx.getProcessIds());
		for (int i = 0; i < idx.size(); i++) {
			LongPair recipient = idx.getProviderAt(i);
			List<CalcExchange> candidates = search.getLinkCandidates(
					exchanges.get(recipient.getFirst()));
			for (CalcExchange linkExchange : candidates) {
				LongPair provider = search.find(linkExchange);
				if (provider == null)
					continue;
				LongPair exchange = new LongPair(recipient.getFirst(),
						linkExchange.exchangeId);
				idx.putLink(exchange, provider);
			}
		}
	}

	private void writeMatrices(InventoryMatrix mat)
			throws Exception {
		Matrices.writeDenseColumn(mat.technologyMatrix, new File(dir, "A.bin"));
		Matrices.writeDenseColumn(mat.interventionMatrix,
				new File(dir, "B.bin"));
		if (withResults) {
			IMatrix invA = solver.invert(mat.technologyMatrix);
			Matrices.writeDenseColumn(invA, new File(dir, "Ainv.bin"));
			IMatrix m = solver.multiply(mat.interventionMatrix, invA);
			Matrices.writeDenseColumn(m, new File(dir, "M.bin"));
		}
	}
}
