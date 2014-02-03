package org.openlca.core.math;

import org.openlca.core.matrix.ImpactMatrix;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.LinkContributions;
import org.openlca.core.results.SimpleResult;

public class LcaCalculator {

	private final IMatrixFactory<?> factory;
	private final IMatrixSolver solver;

	public LcaCalculator(IMatrixSolver solver) {
		this.solver = solver;
		this.factory = solver.getMatrixFactory();
	}

	public SimpleResult calculateSimple(Inventory inventory) {
		return calculateSimple(inventory, null);
	}

	public SimpleResult calculateSimple(Inventory inventory,
			ImpactTable impactTable) {
		InventoryMatrix matrix = inventory.asMatrix(factory);
		ImpactMatrix impactMatrix = impactTable != null ? impactTable
				.asMatrix(factory) : null;
		return calculateSimple(matrix, impactMatrix);
	}

	public SimpleResult calculateSimple(InventoryMatrix matrix) {
		return calculateSimple(matrix, null);
	}

	public SimpleResult calculateSimple(InventoryMatrix matrix,
			ImpactMatrix impactMatrix) {

		SimpleResult result = new SimpleResult();
		result.setFlowIndex(matrix.getFlowIndex());
		result.setProductIndex(matrix.getProductIndex());

		IMatrix techMatrix = matrix.getTechnologyMatrix();
		ProductIndex productIndex = matrix.getProductIndex();
		int idx = productIndex.getIndex(productIndex.getRefProduct());
		double[] s = solver.solve(techMatrix, idx, productIndex.getDemand());
		IMatrix enviMatrix = matrix.getInterventionMatrix();

		double[] g = solver.multiply(enviMatrix, s);
		result.setTotalFlowResults(g);

		if (impactMatrix != null) {
			IMatrix impactFactors = impactMatrix.getFactorMatrix();
			double[] i = solver.multiply(impactFactors, g);
			result.setImpactIndex(impactMatrix.getCategoryIndex());
			result.setTotalImpactResults(i);
		}
		return result;
	}

	public ContributionResult calculateContributions(Inventory inventory) {
		return calculateContributions(inventory, null);
	}

	public ContributionResult calculateContributions(Inventory inventory,
			ImpactTable impactTable) {
		InventoryMatrix matrix = inventory.asMatrix(factory);
		ImpactMatrix impactMatrix = impactTable != null ? impactTable
				.asMatrix(factory) : null;
		return calculateContributions(matrix, impactMatrix);
	}

	public ContributionResult calculateContributions(InventoryMatrix matrix) {
		return calculateContributions(matrix, null);
	}

	public ContributionResult calculateContributions(InventoryMatrix matrix,
			ImpactMatrix impactMatrix) {

		ContributionResult result = new ContributionResult();
		result.setFlowIndex(matrix.getFlowIndex());
		result.setProductIndex(matrix.getProductIndex());

		IMatrix techMatrix = matrix.getTechnologyMatrix();
		ProductIndex productIndex = matrix.getProductIndex();
		int idx = productIndex.getIndex(productIndex.getRefProduct());
		double[] s = solver.solve(techMatrix, idx, productIndex.getDemand());
		result.setScalingFactors(s);

		IMatrix enviMatrix = matrix.getInterventionMatrix();
		IMatrix singleResult = enviMatrix.copy();
		solver.scaleColumns(singleResult, s);
		result.setSingleFlowResults(singleResult);
		double[] g = solver.multiply(enviMatrix, s);
		result.setTotalFlowResults(g);

		LinkContributions linkContributions = LinkContributions.calculate(
				techMatrix, productIndex, s);
		result.setLinkContributions(linkContributions);

		if (impactMatrix != null) {
			IMatrix impactFactors = impactMatrix.getFactorMatrix();
			double[] i = solver.multiply(impactFactors, g);
			result.setImpactIndex(impactMatrix.getCategoryIndex());
			result.setTotalImpactResults(i);
			IMatrix singleImpactResult = solver.multiply(impactFactors,
					singleResult);
			result.setSingleImpactResults(singleImpactResult);

			IMatrix singleFlowImpacts = impactFactors.copy();
			solver.scaleColumns(singleFlowImpacts, g);
			result.setSingleFlowImpacts(singleFlowImpacts);

		}
		return result;
	}

	public FullResult calculateFull(Inventory inventory) {
		return calculateFull(inventory, null);
	}

	public FullResult calculateFull(Inventory inventory, ImpactTable impactTable) {
		InventoryMatrix matrix = inventory.asMatrix(factory);
		ImpactMatrix impactMatrix = impactTable != null ? impactTable
				.asMatrix(factory) : null;
		return calculateFull(matrix, impactMatrix);
	}

	public FullResult calculateFull(InventoryMatrix matrix) {
		return calculateFull(matrix, null);
	}

	public FullResult calculateFull(InventoryMatrix matrix,
			ImpactMatrix impactMatrix) {

		FullResult result = new FullResult();
		result.setFlowIndex(matrix.getFlowIndex());
		result.setProductIndex(matrix.getProductIndex());

		ProductIndex productIndex = matrix.getProductIndex();
		IMatrix techMatrix = matrix.getTechnologyMatrix();
		IMatrix enviMatrix = matrix.getInterventionMatrix();
		IMatrix inverse = solver.invert(techMatrix);
		double[] scalingVector = getScalingVector(inverse, productIndex);
		result.setScalingFactors(scalingVector);

		// single results
		IMatrix singleResult = enviMatrix.copy();
		solver.scaleColumns(singleResult, scalingVector);
		result.setSingleFlowResults(singleResult);

		// total results
		double[] demands = new double[productIndex.size()];
		for (int i = 0; i < productIndex.size(); i++) {
			double entry = techMatrix.getEntry(i, i);
			double s = scalingVector[i];
			demands[i] = s * entry;
		}
		IMatrix totalResult = solver.multiply(enviMatrix, inverse);

		// allow GC
		inverse = null;

		solver.scaleColumns(totalResult, demands);
		result.setUpstreamFlowResults(totalResult);
		int refIdx = productIndex.getIndex(productIndex.getRefProduct());
		double[] g = totalResult.getColumn(refIdx);
		result.setTotalFlowResults(g);

		LinkContributions linkContributions = LinkContributions.calculate(
				techMatrix, productIndex, scalingVector);
		result.setLinkContributions(linkContributions);

		if (impactMatrix != null) {
			result.setImpactIndex(impactMatrix.getCategoryIndex());
			IMatrix factors = impactMatrix.getFactorMatrix();
			IMatrix singleImpactResult = solver.multiply(factors, singleResult);
			result.setSingleImpactResults(singleImpactResult);

			IMatrix singleFlowImpacts = factors.copy();
			solver.scaleColumns(singleFlowImpacts, g);
			result.setSingleFlowImpacts(singleFlowImpacts);

			IMatrix totalImpactResult = solver.multiply(factors, totalResult);
			result.setUpstreamImpactResults(totalImpactResult);
			result.setTotalImpactResults(totalImpactResult.getColumn(refIdx));
		}
		return result;

	}

	private double[] getScalingVector(IMatrix inverse, ProductIndex productIndex) {
		LongPair refProduct = productIndex.getRefProduct();
		int idx = productIndex.getIndex(refProduct);
		double[] s = inverse.getColumn(idx);
		double demand = productIndex.getDemand();
		for (int i = 0; i < s.length; i++)
			s[i] *= demand;
		return s;
	}

}
