package org.openlca.core.math;

import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.results.AnalysisResult;
import org.openlca.core.results.InventoryResult;

/**
 * Calculates inventory and analysis results based on the default matrix methods
 * of the respective matrix implementation.
 */
public class DefaultInventorySolver implements InventorySolver {

	@Override
	public InventoryResult solve(InventoryMatrix matrix, IMatrixFactory factory) {
		IMatrix techMatrix = matrix.getTechnologyMatrix();
		IMatrix demand = Calculators.createDemandVector(
				matrix.getProductIndex(), factory);
		IMatrix s = techMatrix.solve(demand);
		IMatrix enviMatrix = matrix.getInterventionMatrix();
		IMatrix g = enviMatrix.multiply(s);
		InventoryResult result = new InventoryResult();
		result.setFlowIndex(matrix.getFlowIndex());
		result.setFlowResultVector(g);
		result.setProductIndex(matrix.getProductIndex());
		return result;
	}

	@Override
	public AnalysisResult analyse(InventoryMatrix matrix, IMatrixFactory factory) {
		ProductIndex productIndex = matrix.getProductIndex();
		FlowIndex flowIndex = matrix.getFlowIndex();
		IMatrix techMatrix = matrix.getTechnologyMatrix();
		IMatrix enviMatrix = matrix.getInterventionMatrix();

		AnalysisResult result = new AnalysisResult(flowIndex, productIndex);

		IMatrix inverse = techMatrix.getInverse();
		IMatrix demand = Calculators.createDemandVector(productIndex, factory);
		IMatrix scalingFactors = inverse.multiply(demand);
		// we know that the reference product is always in the first column
		result.setScalingFactors(scalingFactors.getColumn(0));

		// single results
		int n = productIndex.size();
		IMatrix scalingMatrix = factory.create(n, n);
		for (int i = 0; i < n; i++) {
			scalingMatrix.setEntry(i, i, scalingFactors.getEntry(i, 0));
		}
		IMatrix singleResult = enviMatrix.multiply(scalingMatrix);
		result.setSingleResult(singleResult);

		// total results
		// TODO: self loop correction
		IMatrix demandMatrix = factory.create(n, n);
		for (int i = 0; i < productIndex.size(); i++) {
			double entry = techMatrix.getEntry(i, i);
			double s = scalingFactors.getEntry(i, 0);
			demandMatrix.setEntry(i, i, s * entry);
		}
		IMatrix totalResult = enviMatrix.multiply(inverse).multiply(
				demandMatrix);
		result.setTotalResult(totalResult);
		return result;
	}

}
