package org.openlca.core.math;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealLinearOperator;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SymmLQ;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.results.InventoryResult;

public class JavaIterativeSolver extends DefaultInventorySolver {

	@Override
	public InventoryResult solve(InventoryMatrix matrix, IMatrixFactory factory) {
		JavaMatrix techMatrix = (JavaMatrix) matrix.getTechnologyMatrix();
		ProductIndex productIndex = matrix.getProductIndex();
		LongPair refProduct = productIndex.getRefProduct();
		int idx = productIndex.getIndex(refProduct);
		ArrayRealVector demand = new ArrayRealVector(productIndex.size());
		demand.setEntry(idx, productIndex.getDemand());
		SymmLQ gradient = new SymmLQ(10000, 1e-5, false);
		RealVector sVector = gradient.solve(
				(RealLinearOperator) techMatrix.getRealMatrix(), demand);
		JavaMatrix s = new JavaMatrix(sVector);
		IMatrix enviMatrix = matrix.getInterventionMatrix();
		IMatrix g = enviMatrix.multiply(s);
		InventoryResult result = new InventoryResult();
		result.setFlowIndex(matrix.getFlowIndex());
		result.setFlowResultVector(g);
		result.setProductIndex(matrix.getProductIndex());
		return result;
	}
}
