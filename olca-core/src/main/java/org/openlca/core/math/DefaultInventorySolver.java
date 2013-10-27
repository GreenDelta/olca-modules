package org.openlca.core.math;

import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.results.AnalysisResult;
import org.openlca.core.results.InventoryResult;

/**
 * Calculates inventory and analysis results based on the default matrix methods
 * of the respective matrix implementation. This class provides several
 * hook-methods that can be overwritten by sub-classes.
 */
public class DefaultInventorySolver implements InventorySolver {

	protected IMatrix techMatrix;
	protected IMatrix enviMatrix;
	protected IMatrix demandVector;

	@Override
	public InventoryResult solve(InventoryMatrix matrix, IMatrixFactory factory) {
		init(matrix, factory);
		IMatrix s = solveScalingVector();
		IMatrix g = enviMatrix.multiply(s);
		InventoryResult result = new InventoryResult();
		result.setFlowIndex(matrix.getFlowIndex());
		result.setFlowResultVector(g);
		result.setProductIndex(matrix.getProductIndex());
		result.setScalingVector(s);
		dispose();
		return result;
	}

	/**
	 * Solves A * s = d ( techMatrix * scalingVector = demandVector) and returns
	 * s.
	 */
	protected IMatrix solveScalingVector() {
		IMatrix s = techMatrix.solve(demandVector);
		return s;
	}

	@Override
	public AnalysisResult analyse(InventoryMatrix matrix, IMatrixFactory factory) {
		init(matrix, factory);
		ProductIndex productIndex = matrix.getProductIndex();
		FlowIndex flowIndex = matrix.getFlowIndex();

		AnalysisResult result = new AnalysisResult(flowIndex, productIndex);

		IMatrix inverse = invertTechMatrix();
		IMatrix scalingFactors = inverse.multiply(demandVector);
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
		dispose();
		return result;
	}

	/**
	 * Calculates the inverse of the technology matrix inv(A)
	 */
	protected IMatrix invertTechMatrix() {
		IMatrix inverse = techMatrix.getInverse();
		return inverse;
	}

	/**
	 * Initializes the temporary fields in this class. This can be overwritten
	 * by a pre-conditioner of the matrices.
	 */
	protected void init(InventoryMatrix matrix, IMatrixFactory factory) {
		techMatrix = matrix.getTechnologyMatrix();
		demandVector = Calculators.createDemandVector(matrix.getProductIndex(),
				factory);
		enviMatrix = matrix.getInterventionMatrix();
	}

	/**
	 * Allow garbage collection of the temporary fields if this solver is still
	 * referenced after a calculation.
	 */
	protected void dispose() {
		techMatrix = null;
		enviMatrix = null;
		demandVector = null;
	}

}
