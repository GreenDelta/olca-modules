package org.openlca.core.math;

import org.openlca.core.matrix.ImpactMatrix;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.results.AnalysisResult;
import org.openlca.core.results.InventoryResult;
import org.openlca.core.results.LinkContributions;

public class InventoryCalculator {

	private final InventorySolver solver;
	private final IMatrixFactory factory;

	public InventoryCalculator(IMatrixFactory factory) {
		this(factory, new DefaultInventorySolver());
	}

	public InventoryCalculator(IMatrixFactory factory, InventorySolver solver) {
		this.factory = factory;
		this.solver = solver;
	}

	public InventoryResult solve(Inventory inventory) {
		return solve(inventory, null);
	}

	public InventoryResult solve(Inventory inventory, ImpactTable impactTable) {
		InventoryMatrix matrix = inventory.asMatrix(factory);
		ImpactMatrix impactMatrix = impactTable != null ? impactTable
				.asMatrix(factory) : null;
		return solve(matrix, impactMatrix);
	}

	public InventoryResult solve(InventoryMatrix matrix) {
		return solve(matrix, null);
	}

	public InventoryResult solve(InventoryMatrix matrix,
			ImpactMatrix impactMatrix) {
		InventoryResult result = solver.solve(matrix, factory);
		if (impactMatrix != null) {
			IMatrix impactFactors = impactMatrix.getFactorMatrix();
			IMatrix i = impactFactors.multiply(result.getFlowResultVector());
			result.setImpactIndex(impactMatrix.getCategoryIndex());
			result.setImpactResultVector(i);
		}
		return result;
	}

	public AnalysisResult analyse(Inventory inventory) {
		return analyse(inventory, null);
	}

	public AnalysisResult analyse(Inventory inventory, ImpactTable impactTable) {
		InventoryMatrix matrix = inventory.asMatrix(factory);
		ImpactMatrix impactMatrix = impactTable != null ? impactTable
				.asMatrix(factory) : null;
		return analyse(matrix, impactMatrix);
	}

	public AnalysisResult analyse(InventoryMatrix matrix) {
		return analyse(matrix, null);
	}

	public AnalysisResult analyse(InventoryMatrix matrix,
			ImpactMatrix impactMatrix) {
		AnalysisResult result = solver.analyse(matrix, factory);
		LinkContributions linkContributions = LinkContributions.calculate(
				matrix.getTechnologyMatrix(), matrix.getProductIndex(),
				result.getScalingFactors());
		result.setLinkContributions(linkContributions);
		if (impactMatrix != null) {
			result.setImpactCategoryIndex(impactMatrix.getCategoryIndex());
			IMatrix factors = impactMatrix.getFactorMatrix();
			result.setImpactFactors(factors);
			IMatrix singleImpactResult = factors.multiply(result
					.getSingleResult());
			result.setSingleImpactResult(singleImpactResult);
			IMatrix totalImpactResult = factors.multiply(result
					.getTotalResult());
			result.setTotalImpactResult(totalImpactResult);
		}
		return result;

	}

}
