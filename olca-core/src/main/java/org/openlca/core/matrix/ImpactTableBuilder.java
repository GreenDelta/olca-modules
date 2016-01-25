package org.openlca.core.matrix;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openlca.core.matrix.cache.MatrixCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Longs;

class ImpactTableBuilder {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final MatrixCache cache;
	private final long methodId;
	private final FlowIndex flowIndex;

	ImpactTableBuilder(MatrixCache cache, long impactMethodId,
			FlowIndex flowIndex) {
		this.cache = cache;
		this.methodId = impactMethodId;
		this.flowIndex = flowIndex;
	}

	ImpactTable build() {
		log.trace("Build impact factor matrix for method {}", methodId);
		LongIndex categoryIndex = buildCategoryIndex();
		if (categoryIndex.isEmpty() || flowIndex.isEmpty())
			return null;
		ImpactTable table = new ImpactTable();
		table.categoryIndex = categoryIndex;
		table.flowIndex = flowIndex;
		ImpactFactorMatrix matrix = new ImpactFactorMatrix(
				categoryIndex.size(), flowIndex.size());
		table.factorMatrix = matrix;
		fill(matrix, categoryIndex);
		log.trace("Impact factor matrix ready");
		return table;
	}

	private LongIndex buildCategoryIndex() {
		LongIndex index = new LongIndex();
		try (Connection con = cache.getDatabase().createConnection()) {
			String query = "select id from tbl_impact_categories where f_impact_method = "
					+ methodId;
			ResultSet result = con.createStatement().executeQuery(query);
			while (result.next()) {
				long id = result.getLong("id");
				index.put(id);
			}
			result.close();
		} catch (Exception e) {
			log.error("failed to build impact category index", e);
		}
		return index;
	}

	private void fill(ImpactFactorMatrix matrix, LongIndex categoryIndex) {
		Map<Long, List<CalcImpactFactor>> factorMap = loadFactors(categoryIndex);
		for (int row = 0; row < categoryIndex.size(); row++) {
			long categoryId = categoryIndex.getKeyAt(row);
			List<CalcImpactFactor> factors = factorMap.get(categoryId);
			if (factors == null)
				continue;
			for (CalcImpactFactor factor : factors) {
				long flowId = factor.getFlowId();
				int col = flowIndex.getIndex(flowId);
				if (col < 0)
					continue;
				boolean input = flowIndex.isInput(flowId);
				ImpactFactorCell cell = new ImpactFactorCell(factor, methodId,
						input);
				matrix.setEntry(row, col, cell);
			}
		}
	}

	private Map<Long, List<CalcImpactFactor>> loadFactors(
			LongIndex categoryIndex) {
		try {
			return cache.getImpactCache().getAll(
					Longs.asList(categoryIndex.getKeys()));
		} catch (Exception e) {
			log.error("failed to load impact factors");
			return Collections.emptyMap();
		}
	}

}
