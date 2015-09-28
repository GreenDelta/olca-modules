package org.openlca.core.matrix;

import java.util.List;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.IMatrix;
import org.openlca.core.math.IMatrixFactory;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.CostCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import gnu.trove.map.hash.TLongObjectHashMap;

class CostMatrixBuilder {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final MatrixCache matrixCache;
	private final IMatrixFactory<?> factory;
	private final ProductIndex productIndex;

	private TLongObjectHashMap<CostCategory> costCategories;
	private Multimap<LongPair, CalcCostEntry> costEntries = HashMultimap
			.create();
	private LongIndex fixCostCategoryIndex;
	private LongIndex varCostCategoryIndex;

	CostMatrixBuilder(MatrixCache matrixCache, IMatrixFactory<?> factory,
			ProductIndex productIndex) {
		this.matrixCache = matrixCache;
		this.factory = factory;
		this.productIndex = productIndex;
	}

	CostMatrix build() {
		log.trace("build cost matrix");
		CostMatrix costMatrix = new CostMatrix();
		costMatrix.setProductIndex(productIndex);
		costCategories = loadCostCategories();
		fixCostCategoryIndex = new LongIndex();
		varCostCategoryIndex = new LongIndex();
		indexData();
		if (!fixCostCategoryIndex.isEmpty()) {
			IMatrix fixCosts = buildMatrix(fixCostCategoryIndex);
			costMatrix.setFixCosts(fixCostCategoryIndex, fixCosts);
		}
		if (!varCostCategoryIndex.isEmpty()) {
			IMatrix varCosts = buildMatrix(varCostCategoryIndex);
			costMatrix.setVariableCosts(varCostCategoryIndex, varCosts);
		}
		return costMatrix;
	}

	private TLongObjectHashMap<CostCategory> loadCostCategories() {
		IDatabase database = matrixCache.getDatabase();
		TLongObjectHashMap<CostCategory> index = new TLongObjectHashMap<>();
		List<CostCategory> list = database.createDao(CostCategory.class)
				.getAll();
		for (CostCategory cat : list)
			index.put(cat.getId(), cat);
		return index;
	}

	private void indexData() {
		try {
			Map<Long, List<CalcCostEntry>> lists = matrixCache.getCostCache()
					.getAll(productIndex.getProcessIds());
			for (List<CalcCostEntry> list : lists.values()) {
				for (CalcCostEntry entry : list)
					indexEntry(entry);
			}
		} catch (Exception e) {
			log.error("failed to load cost entruies from database", e);
		}
	}

	private void indexEntry(CalcCostEntry entry) {
		LongPair processProduct = LongPair.of(entry.getProcessId(),
				entry.getExchangeId());
		if (!productIndex.contains(processProduct))
			return;
		CostCategory cat = costCategories.get(entry.getCostCategoryId());
		if (cat == null)
			return;
		varCostCategoryIndex.put(cat.getId());
		costEntries.put(processProduct, entry);
	}

	private IMatrix buildMatrix(LongIndex costCategoryIndex) {
		IMatrix matrix = factory.create(costCategoryIndex.size(),
				productIndex.size());
		for (int col = 0; col < productIndex.size(); col++) {
			LongPair processProduct = productIndex.getProductAt(col);
			for (CalcCostEntry entry : costEntries.get(processProduct)) {
				if (!costCategoryIndex.contains(entry.getCostCategoryId()))
					continue;
				int row = costCategoryIndex.getIndex(entry.getCostCategoryId());
				double val = entry.getAmount();
				matrix.setEntry(row, col, val);
			}
		}
		return matrix;
	}

}
