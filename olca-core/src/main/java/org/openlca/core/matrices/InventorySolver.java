package org.openlca.core.matrices;

import org.openlca.core.indices.ProductIndex;
import org.openlca.core.math.IMatrix;
import org.openlca.core.math.MatrixFactory;

public class InventorySolver {

	public double[] solve(InventoryMatrix inventory) {
		if (inventory.isEmpty())
			return new double[0];

		IMatrix techMatrix = inventory.getTechnologyMatrix();
		IMatrix interventionMatrix = inventory.getInterventionMatrix();
		ProductIndex productIndex = inventory.getProductIndex();

		IMatrix demandVector = MatrixFactory.create(
				techMatrix.getRowDimension(), 1);
		int refIndex = productIndex.getIndex(productIndex.getRefProduct());
		demandVector.setEntry(refIndex, 0, 1);

		IMatrix s = techMatrix.solve(demandVector);
		IMatrix g = interventionMatrix.multiply(s);
		return g.getColumn(0);
	}

}
