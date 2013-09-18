package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.CostMatrix;
import org.openlca.core.matrix.CostMatrixBuilder;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.results.CostResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A calculator for life cycle costs of a product system. */
public class CostCalculator {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;

	public CostCalculator(IDatabase database) {
		this.database = database;
	}

	/**
	 * Calculates cost results for the product system result, defined by the
	 * given product index and scaling vector.
	 */
	public CostResult calculate(ProductIndex index, double[] s) {
		CostResult result = new CostResult();
		result.setProductIndex(index);
		try {
			CostMatrixBuilder builder = new CostMatrixBuilder(database);
			CostMatrix matrix = builder.build(index);
			if (matrix.hasVarCosts())
				calculateVarCosts(matrix, s, result);
			if (matrix.hasFixCosts())
				calculateFixCosts(matrix, s, result);
		} catch (Exception e) {
			log.error("failed to calculate LCC results", e);
		}
		return result;
	}

	private void calculateVarCosts(CostMatrix matrix, double[] s,
			CostResult result) {
		IMatrix varCosts = matrix.getVariableCostMatrix();
		IMatrix scalingVector = MatrixFactory.create(s.length, 1);
		for (int row = 0; row < s.length; row++)
			scalingVector.setEntry(row, 0, s[row]);
		IMatrix varResult = varCosts.multiply(scalingVector);
		result.setVarCostCategoryIndex(matrix.getVarCostCategoryIndex());
		result.setVarCostResults(varResult.getColumn(0));
	}

	private void calculateFixCosts(CostMatrix matrix, double[] s,
			CostResult result) {
		IMatrix fixCosts = matrix.getFixCostMatrix();
		double[] fixResult = new double[fixCosts.getRowDimension()];
		for (int row = 0; row < fixCosts.getRowDimension(); row++) {
			for (int col = 0; col < fixCosts.getColumnDimension(); col++) {
				double val = fixCosts.getEntry(row, col);
				fixResult[row] += val;
			}
		}
		result.setFixCostCategoryIndex(matrix.getFixCostCategoryIndex());
		result.setFixCostResults(fixResult);
	}

}
