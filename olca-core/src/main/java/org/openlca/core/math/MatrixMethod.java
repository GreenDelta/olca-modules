package org.openlca.core.math;

import org.openlca.core.model.ProductSystem;

/**
 * Some helper methods for the matrix method.
 */
class MatrixMethod {

	public static IMatrix demandVector(ProductSystem system,
			InventoryMatrix matrix) {
		int idx = getReferenceIndex(system, matrix);
		double amount = system.getConvertedTargetAmount();
		IMatrix techMatrix = matrix.getTechnologyMatrix();
		IMatrix demandVector = MatrixFactory.create(
				techMatrix.getRowDimension(), 1);
		demandVector.setEntry(idx, 0, amount);
		return demandVector;
	}

	/** Get the index of the reference product in the matrix. */
	public static int getReferenceIndex(ProductSystem system,
			InventoryMatrix matrix) {
		ProductIndex productIndex = matrix.getProductIndex();
		int idx = productIndex.getIndex(system.getReferenceProcess(),
				system.getReferenceExchange());
		return idx;
	}

	/**
	 * Simple calculation of a product system. Returns the result vector g as an
	 * array of doubles (negative values = inputs, positive values = outputs).
	 */
	public static double[] solve(ProductSystem system, InventoryMatrix matrix) {
		if (matrix.isEmpty())
			return new double[0];
		IMatrix demand = MatrixMethod.demandVector(system, matrix);
		IMatrix techMatrix = matrix.getTechnologyMatrix();
		IMatrix s = techMatrix.solve(demand);
		IMatrix g = matrix.getInterventionMatrix().multiply(s);
		return g.getColumn(0);
	}

}
