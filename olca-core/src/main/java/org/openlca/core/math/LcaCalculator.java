package org.openlca.core.math;

import org.openlca.core.matrix.ImpactMatrix;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.LinkContributions;
import org.openlca.core.results.SimpleResult;

public class LcaCalculator {

	private final IMatrixSolver solver;

	public LcaCalculator(IMatrixSolver solver) {
		this.solver = solver;
	}

	public SimpleResult calculateSimple(InventoryMatrix matrix) {
		return calculateSimple(matrix, null);
	}

	public SimpleResult calculateSimple(InventoryMatrix matrix,
			ImpactMatrix impactMatrix) {

		SimpleResult result = new SimpleResult();
		result.flowIndex = matrix.getFlowIndex();
		result.productIndex = matrix.getProductIndex();

		IMatrix techMatrix = matrix.getTechnologyMatrix();
		ProductIndex productIndex = matrix.getProductIndex();
		int idx = productIndex.getIndex(productIndex.getRefProduct());
		double[] s = solver.solve(techMatrix, idx, productIndex.getDemand());
		IMatrix enviMatrix = matrix.getInterventionMatrix();

		double[] g = solver.multiply(enviMatrix, s);
		result.totalFlowResults = g;

		if (impactMatrix != null) {
			IMatrix impactFactors = impactMatrix.getFactorMatrix();
			double[] i = solver.multiply(impactFactors, g);
			result.impactIndex = impactMatrix.getCategoryIndex();
			result.totalImpactResults = i;
		}
		return result;
	}

	public ContributionResult calculateContributions(InventoryMatrix matrix) {
		return calculateContributions(matrix, null);
	}

	public ContributionResult calculateContributions(InventoryMatrix matrix,
			ImpactMatrix impactMatrix) {

		ContributionResult result = new ContributionResult();
		result.flowIndex = matrix.getFlowIndex();
		result.productIndex = matrix.getProductIndex();

		IMatrix techMatrix = matrix.getTechnologyMatrix();
		ProductIndex productIndex = matrix.getProductIndex();
		int idx = productIndex.getIndex(productIndex.getRefProduct());
		double[] s = solver.solve(techMatrix, idx, productIndex.getDemand());
		result.scalingFactors = s;

		IMatrix enviMatrix = matrix.getInterventionMatrix();
		IMatrix singleResult = enviMatrix.copy();
		solver.scaleColumns(singleResult, s);
		result.singleFlowResults = singleResult;
		double[] g = solver.multiply(enviMatrix, s);
		result.totalFlowResults = g;

		LinkContributions linkContributions = LinkContributions.calculate(
				techMatrix, productIndex, s);
		result.linkContributions = linkContributions;

		if (impactMatrix != null) {
			result.impactIndex = impactMatrix.getCategoryIndex();
			IMatrix impactFactors = impactMatrix.getFactorMatrix();
			result.impactFactors = impactFactors;
			double[] i = solver.multiply(impactFactors, g);
			result.totalImpactResults = i;
			IMatrix singleImpactResult = solver.multiply(impactFactors,
					singleResult);
			result.singleImpactResults = singleImpactResult;

			IMatrix singleFlowImpacts = impactFactors.copy();
			solver.scaleColumns(singleFlowImpacts, g);
			result.singleFlowImpacts = singleFlowImpacts;

		}
		return result;
	}

	public FullResult calculateFull(InventoryMatrix matrix) {
		return calculateFull(matrix, null);
	}

	public FullResult calculateFull(InventoryMatrix matrix,
			ImpactMatrix impactMatrix) {

		FullResult result = new FullResult();
		result.flowIndex = matrix.getFlowIndex();
		result.productIndex = matrix.getProductIndex();

		ProductIndex productIndex = matrix.getProductIndex();
		IMatrix techMatrix = matrix.getTechnologyMatrix();
		IMatrix enviMatrix = matrix.getInterventionMatrix();
		IMatrix inverse = solver.invert(techMatrix);
		double[] scalingVector = getScalingVector(inverse, productIndex);
		result.scalingFactors = scalingVector;

		// single results
		IMatrix singleResult = enviMatrix.copy();
		solver.scaleColumns(singleResult, scalingVector);
		result.singleFlowResults = singleResult;

		// total results
		double[] demands = new double[productIndex.size()];
		for (int i = 0; i < productIndex.size(); i++) {
			double entry = techMatrix.getEntry(i, i);
			double s = scalingVector[i];
			demands[i] = s * entry;
		}
		int idx = productIndex.getIndex(productIndex.getRefProduct());
		if (Math.abs(demands[idx] - productIndex.getDemand()) > 1e-9) {
			// 'self-loop' correction for total result scale
			double f = productIndex.getDemand() / demands[idx];
			for (int k = 0; k < scalingVector.length; k++)
				demands[k] = demands[k] * f;
		}

		IMatrix totalResult = solver.multiply(enviMatrix, inverse);
		inverse = null; // allow GC
		solver.scaleColumns(totalResult, demands);
		result.upstreamFlowResults = totalResult;
		int refIdx = productIndex.getIndex(productIndex.getRefProduct());
		double[] g = totalResult.getColumn(refIdx);
		result.totalFlowResults = g;

		LinkContributions linkContributions = LinkContributions.calculate(
				techMatrix, productIndex, scalingVector);
		result.linkContributions = linkContributions;

		if (impactMatrix != null) {
			result.impactIndex = impactMatrix.getCategoryIndex();
			IMatrix factors = impactMatrix.getFactorMatrix();
			result.impactFactors = factors;
			IMatrix singleImpactResult = solver.multiply(factors, singleResult);
			result.singleImpactResults = singleImpactResult;

			IMatrix singleFlowImpacts = factors.copy();
			solver.scaleColumns(singleFlowImpacts, g);
			result.singleFlowImpacts = singleFlowImpacts;

			IMatrix totalImpactResult = solver.multiply(factors, totalResult);
			result.upstreamImpactResults = totalImpactResult;
			result.totalImpactResults = totalImpactResult.getColumn(refIdx);
		}
		return result;

	}

	private double[] getScalingVector(IMatrix inverse,
			ProductIndex productIndex) {
		LongPair refProduct = productIndex.getRefProduct();
		int idx = productIndex.getIndex(refProduct);
		double[] s = inverse.getColumn(idx);
		double demand = productIndex.getDemand();
		for (int i = 0; i < s.length; i++)
			s[i] *= demand;
		return s;
	}

}
