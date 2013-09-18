package org.openlca.core.matrix;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openlca.core.matrix.cache.MatrixCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Longs;

/**
 * Builds a matrix with impact assessment factors.
 * 
 */
public class ImpactMatrixBuilder {

	private MatrixCache cache;
	private Logger log = LoggerFactory.getLogger(getClass());

	public ImpactMatrixBuilder(MatrixCache cache) {
		this.cache = cache;
	}

	public ImpactMatrix build(long impactMethodId, FlowIndex flowIndex) {
		log.trace("Build impact factor matrix for method {}", impactMethodId);
		LongIndex categoryIndex = buildCategoryIndex(impactMethodId);
		if (categoryIndex.isEmpty() || flowIndex.isEmpty())
			return null;
		ImpactMatrix matrix = new ImpactMatrix(categoryIndex.size(),
				flowIndex.size());
		List<Long> impactCategoryIds = new ArrayList<>();
		for (long categoryId : categoryIndex.getKeys())
			impactCategoryIds.add(categoryId);
		fill(matrix, flowIndex, categoryIndex);
		log.trace("Impact factor matrix ready");
		return matrix;
	}

	private LongIndex buildCategoryIndex(long methodId) {
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

	private void fill(ImpactMatrix matrix, FlowIndex flowIndex,
			LongIndex categoryIndex) {
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
				ImpactFactorCell cell = new ImpactFactorCell(factor, input);
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
