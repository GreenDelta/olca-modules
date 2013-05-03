package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.results.SimpleCostResult;
import org.slf4j.LoggerFactory;

// TODO: no allocation methods yet
public class CostMatrixSolver {

	private IDatabase database;

	public CostMatrixSolver(IDatabase database) {
		this.database = database;
	}

	public SimpleCostResult calculate(ProductSystem system) {
		CostMatrixBuilder builder = new CostMatrixBuilder(system, database);
		CostMatrix matrix = builder.build();
		if (matrix.isEmpty())
			return new SimpleCostResult(null, new double[0]);
		String refProduct = system.getReferenceExchange().getId();
		double refAmount = system.getConvertedTargetAmount();
		double[] costs = doCalc(matrix, refProduct, refAmount);
		return new SimpleCostResult(matrix.getCostCategoryIndex(), costs);
	}

	private double[] doCalc(CostMatrix matrix, String refProduct,
			double refAmount) {
		try {
			IMatrix techMatrix = matrix.getTechnologyMatrix();
			IMatrix costMatrix = matrix.getCostMatrix();
			int demandIdx = matrix.getProductIndex().getIndex(refProduct);
			IMatrix demandVector = MatrixFactory.create(
					techMatrix.getRowDimension(), 1);
			demandVector.setEntry(demandIdx, 0, refAmount);
			IMatrix s = techMatrix.solve(demandVector);
			IMatrix costResults = costMatrix.multiply(s);
			double[] costs = costResults.getColumn(0);
			return costs;
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).error(
					"cost calculation failed", e);
			return new double[0];
		}
	}

}
