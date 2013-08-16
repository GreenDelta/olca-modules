package org.openlca.core.matrices;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.IMatrix;
import org.openlca.core.math.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a matrix with impact assessment factors.
 * 
 */
public class ImpactMatrixBuilder {

	private IDatabase database;
	private Logger log = LoggerFactory.getLogger(getClass());

	public ImpactMatrixBuilder(IDatabase database) {
		this.database = database;
	}

	public ImpactMatrix build(long impactMethodId, FlowIndex flowIndex) {
		log.trace("Build impact factor matrix for method {}", impactMethodId);
		LongIndex categoryIndex = buildCategoryIndex(impactMethodId);
		if (categoryIndex.isEmpty() || flowIndex.isEmpty())
			return null;
		ImpactMatrix matrix = new ImpactMatrix();
		matrix.setCategoryIndex(categoryIndex);
		matrix.setFlowIndex(flowIndex);
		IMatrix values = MatrixFactory.create(categoryIndex.size(),
				flowIndex.size());
		matrix.setValues(values);

		List<Long> impactCategoryIds = new ArrayList<>();
		for (long categoryId : categoryIndex.getKeys())
			impactCategoryIds.add(categoryId);
		ImpactFactorTable factorTable = new ImpactFactorTable(database,
				impactCategoryIds);
		fill(matrix, factorTable);
		log.trace("Impact factor matrix ready");
		return matrix;
	}

	private LongIndex buildCategoryIndex(long methodId) {
		LongIndex index = new LongIndex();
		try (Connection con = database.createConnection()) {
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

	private void fill(ImpactMatrix matrix, ImpactFactorTable factorTable) {
		FlowIndex flowIndex = matrix.getFlowIndex();
		LongIndex categoryIndex = matrix.getCategoryIndex();
		IMatrix values = matrix.getValues();
		for (int row = 0; row < categoryIndex.size(); row++) {
			long categoryId = categoryIndex.getKeyAt(row);
			List<CalcImpactFactor> factors = factorTable
					.getImpactFactors(categoryId);
			for (CalcImpactFactor factor : factors) {
				long flowId = factor.getFlowId();
				int col = flowIndex.getIndex(flowId);
				if (col < 0)
					continue;
				double val = factor.getAmount() * factor.getConversionFactor();
				if (flowIndex.isInput(flowId))
					val = -val;
				values.setEntry(row, col, val);
			}
		}
	}

}
