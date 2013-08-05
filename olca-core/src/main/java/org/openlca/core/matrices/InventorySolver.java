package org.openlca.core.matrices;

import org.openlca.core.indices.LongPair;
import org.openlca.core.indices.ProductIndex;
import org.openlca.core.math.IMatrix;
import org.openlca.core.math.MatrixFactory;
import org.openlca.core.results.InventoryResult;

public class InventorySolver {

	public InventoryResult solve(Inventory inventory) {
		return solve(inventory, null);
	}

	public InventoryResult solve(Inventory inventory, ImpactMatrix impactMatrix) {

		IMatrix techMatrix = inventory.getTechnologyMatrix().createRealMatrix();
		IMatrix demand = demandVector(inventory);
		IMatrix s = techMatrix.solve(demand);
		IMatrix enviMatrix = inventory.getInterventionMatrix()
				.createRealMatrix();
		IMatrix g = enviMatrix.multiply(s);

		InventoryResult result = new InventoryResult();
		result.setFlowIndex(inventory.getFlowIndex());
		result.setFlowResults(g.getColumn(0));
		result.setProductIndex(inventory.getProductIndex());
		result.setScalingFactors(s.getColumn(0));
		if (impactMatrix != null) {
			IMatrix impactFactors = impactMatrix.getValues();
			IMatrix i = impactFactors.multiply(g);
			result.setImpactIndex(impactMatrix.getCategoryIndex());
			result.setImpactResults(i.getColumn(0));
		}
		return result;
	}

	private IMatrix demandVector(Inventory inventory) {
		ProductIndex index = inventory.getProductIndex();
		LongPair refProduct = index.getRefProduct();
		int idx = index.getIndex(refProduct);
		IMatrix demandVector = MatrixFactory.create(index.size(), 1);
		demandVector.setEntry(idx, 0, index.getDemand());
		return demandVector;
	}

}
