package org.openlca.core.math;

import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.ImpactMatrix;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.results.AnalysisResult;
import org.openlca.core.results.InventoryResult;
import org.openlca.core.results.LinkContributions;

public class InventoryCalculator {

	private final IMatrixFactory<?> factory;
	private final IMatrixSolver solver;

	public InventoryCalculator(IMatrixSolver solver) {
		this.solver = solver;
		this.factory = solver.getMatrixFactory();
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
		IMatrix techMatrix = matrix.getTechnologyMatrix();
		ProductIndex productIndex = matrix.getProductIndex();
		int idx = productIndex.getIndex(productIndex.getRefProduct());
		double[] s = solver.solve(techMatrix, idx, productIndex.getDemand());
		IMatrix enviMatrix = matrix.getInterventionMatrix();
		double[] g = solver.multiply(enviMatrix, s);

		InventoryResult result = new InventoryResult();
		result.setFlowIndex(matrix.getFlowIndex());
		result.setFlowResultVector(g);
		result.setProductIndex(matrix.getProductIndex());
		if (impactMatrix != null) {
			IMatrix impactFactors = impactMatrix.getFactorMatrix();
			double[] i = solver.multiply(impactFactors, g);
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

		ProductIndex productIndex = matrix.getProductIndex();
		FlowIndex flowIndex = matrix.getFlowIndex();
		AnalysisResult result = new AnalysisResult(flowIndex, productIndex);

		IMatrix techMatrix = matrix.getTechnologyMatrix();
		IMatrix enviMatrix = matrix.getInterventionMatrix();

		IMatrix inverse = solver.invert(techMatrix);

		IMatrix demand = createDemandVector(productIndex);
		IMatrix scalingFactors = solver.multiply(inverse, demand);
		// we now that the reference product is always in the first column
		result.setScalingFactors(scalingFactors.getColumn(0));

		// single results
		int n = productIndex.size();
		IMatrix scalingMatrix = factory.create(n, n);
		for (int i = 0; i < n; i++) {
			scalingMatrix.setEntry(i, i, scalingFactors.getEntry(i, 0));
		}
		IMatrix singleResult = solver.multiply(enviMatrix, scalingMatrix);
		result.setSingleResult(singleResult);

		// total results
		// TODO: self loop correction
		IMatrix demandMatrix = factory.create(n, n);
		for (int i = 0; i < productIndex.size(); i++) {
			double entry = techMatrix.getEntry(i, i);
			double s = scalingFactors.getEntry(i, 0);
			demandMatrix.setEntry(i, i, s * entry);
		}
		IMatrix totalResult = solver.multiply(
				solver.multiply(enviMatrix, inverse), demandMatrix);
		result.setTotalResult(totalResult);

		LinkContributions linkContributions = LinkContributions.calculate(
				techMatrix, productIndex, scalingFactors.getColumn(0));
		result.setLinkContributions(linkContributions);

		if (impactMatrix != null) {
			result.setImpactCategoryIndex(impactMatrix.getCategoryIndex());
			IMatrix factors = impactMatrix.getFactorMatrix();
			result.setImpactFactors(factors);
			IMatrix singleImpactResult = solver.multiply(factors, singleResult);
			result.setSingleImpactResult(singleImpactResult);
			IMatrix totalImpactResult = solver.multiply(factors, totalResult);
			result.setTotalImpactResult(totalImpactResult);
		}
		return result;

	}

	private IMatrix createDemandVector(ProductIndex productIndex) {
		LongPair refProduct = productIndex.getRefProduct();
		int idx = productIndex.getIndex(refProduct);
		IMatrix demandVector = factory.create(productIndex.size(), 1);
		demandVector.setEntry(idx, 0, productIndex.getDemand());
		return demandVector;
	}

}
