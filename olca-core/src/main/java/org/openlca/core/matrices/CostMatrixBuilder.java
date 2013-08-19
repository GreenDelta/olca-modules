package org.openlca.core.matrices;

import gnu.trove.map.hash.TLongObjectHashMap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.IMatrix;
import org.openlca.core.math.MatrixFactory;
import org.openlca.core.model.CostCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class CostMatrixBuilder {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final IDatabase database;
	private Multimap<LongPair, CalcCostEntry> costEntries;
	private TLongObjectHashMap<CostCategory> costCategories;
	private LongIndex fixCostCategoryIndex;
	private LongIndex varCostCategoryIndex;

	public CostMatrixBuilder(IDatabase database) {
		this.database = database;
	}

	public CostMatrix build(ProductIndex productIndex) {
		log.trace("build cost matrix");
		CostMatrix costMatrix = new CostMatrix();
		costMatrix.setProductIndex(productIndex);
		costEntries = ArrayListMultimap.create();
		costCategories = loadCostCategories();
		fixCostCategoryIndex = new LongIndex();
		varCostCategoryIndex = new LongIndex();
		loadIndexEntries(productIndex);
		if (!fixCostCategoryIndex.isEmpty()) {
			IMatrix fixCosts = buildMatrix(fixCostCategoryIndex, productIndex);
			costMatrix.setFixCosts(fixCostCategoryIndex, fixCosts);
		}
		if (!varCostCategoryIndex.isEmpty()) {
			IMatrix varCosts = buildMatrix(varCostCategoryIndex, productIndex);
			costMatrix.setVariableCosts(varCostCategoryIndex, varCosts);
		}
		return costMatrix;
	}

	private TLongObjectHashMap<CostCategory> loadCostCategories() {
		TLongObjectHashMap<CostCategory> index = new TLongObjectHashMap<>();
		List<CostCategory> list = database.createDao(CostCategory.class)
				.getAll();
		for (CostCategory cat : list)
			index.put(cat.getId(), cat);
		return index;
	}

	private void loadIndexEntries(ProductIndex productIndex) {
		log.trace("load process cost entries");
		try (Connection con = database.createConnection()) {
			String query = "select * from tbl_process_cost_entries where f_process in "
					+ Indices.asSql(productIndex.getProcessIds());
			ResultSet result = con.createStatement().executeQuery(query);
			while (result.next()) {
				CalcCostEntry costEntry = fetchEntry(result);
				index(productIndex, costEntry);
			}
			result.close();
		} catch (Exception e) {
			log.error("failed to load cost category entries from database", e);
		}
	}

	private CalcCostEntry fetchEntry(ResultSet result) throws Exception {
		CalcCostEntry entry = new CalcCostEntry();
		entry.setAmount(result.getDouble("amount"));
		entry.setCostCategoryId(result.getLong("f_cost_category"));
		entry.setExchangeId(result.getLong("f_exchange"));
		entry.setProcessId(result.getLong("f_process"));
		return entry;
	}

	private void index(ProductIndex productIndex, CalcCostEntry costEntry) {
		LongPair processProduct = new LongPair(costEntry.getProcessId(),
				costEntry.getExchangeId());
		if (!productIndex.contains(processProduct))
			return;
		CostCategory cat = costCategories.get(costEntry.getCostCategoryId());
		if (cat == null)
			return;
		if (cat.isFix())
			fixCostCategoryIndex.put(cat.getId());
		else
			varCostCategoryIndex.put(cat.getId());
		costEntries.put(processProduct, costEntry);
	}

	private IMatrix buildMatrix(LongIndex costCategoryIndex,
			ProductIndex productIndex) {
		IMatrix matrix = MatrixFactory.create(costCategoryIndex.size(),
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
