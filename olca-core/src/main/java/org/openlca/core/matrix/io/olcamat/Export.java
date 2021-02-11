package org.openlca.core.matrix.io.olcamat;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.InventoryBuilder;
import org.openlca.core.matrix.InventoryConfig;
import org.openlca.core.matrix.LinkingConfig;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.product.index.ProviderSearch;
import org.openlca.core.model.ProcessType;

/**
 * Exports a matrix into the openLCA matrix (=olcamat) format.The olcamat format
 * is still experimental so this may change in future.
 */
public class Export implements Runnable {

	private final CalculationSetup setup;
	private final IDatabase db;
	private final MatrixCache cache;
	private final File dir;


	public Export(IDatabase db, File dir) {
		this(null, db, dir);
	}

	public Export(CalculationSetup setup, IDatabase db, File dir) {
		this.setup = setup;
		this.db = db;
		cache = MatrixCache.createEager(db);
		this.dir = dir;
	}

	@Override
	public void run() {
		try {
			if (!dir.exists()) {
				dir.mkdirs();
			}
			MatrixData data;
			if (setup == null) {
				data = dbInventory();
			} else {
				data = setupInventory();
			}
			writeMatrices(data);
			IndexWriter iw = new IndexWriter(data);
			iw.write(db, dir);
		} catch (Exception e) {
			throw new RuntimeException("Export failed", e);
		}
	}

	private MatrixData dbInventory() throws Exception {
		List<ProcessProduct> products = cache.getProcessTable().getProviders();
		TechIndex techIndex = new TechIndex(products.get(0));
		for (int i = 1; i < products.size(); i++) {
			techIndex.put(products.get(i));
		}
		dbLinks(techIndex);
		var config = InventoryConfig.of(db, techIndex)
				.withInterpreter(MatrixData.interpreter(db, setup, techIndex))
				.create();
		var builder = new InventoryBuilder(config);
		return builder.build();
	}

	private MatrixData setupInventory() {
		return MatrixData.of(cache.getDatabase(), setup);
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
			ProcessProduct recipient = idx.getProviderAt(i);
			List<CalcExchange> candidates = search.getLinkCandidates(
					exchanges.get(recipient.id()));
			for (CalcExchange linkExchange : candidates) {
				ProcessProduct provider = search.find(linkExchange);
				if (provider == null)
					continue;
				LongPair exchange = new LongPair(recipient.id(),
						linkExchange.exchangeId);
				idx.putLink(exchange, provider);
			}
		}
	}

	private void writeMatrices(MatrixData mat)
			throws Exception {
		Matrices.writeDenseColumn(mat.techMatrix, new File(dir, "A.bin"));
		Matrices.writeDenseColumn(mat.flowMatrix, new File(dir, "B.bin"));
	}
}
