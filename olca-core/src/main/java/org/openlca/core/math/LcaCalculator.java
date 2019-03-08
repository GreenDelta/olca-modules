package org.openlca.core.math;

import org.openlca.core.matrix.CostVector;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;

/**
 * This calculator does the low level matrix based LCA-calculation. Typically,
 * you do not want to use this directly but a more high level calculator where
 * you can directly throw in a calculation setup or project.
 */
public class LcaCalculator {

	private final IMatrixSolver solver;
	private final MatrixData data;

	public LcaCalculator(IMatrixSolver solver, MatrixData data) {
		this.solver = solver;
		this.data = data;
	}

	public SimpleResult calculateSimple() {

		SimpleResult result = new SimpleResult();
		result.flowIndex = data.enviIndex;
		result.techIndex = data.techIndex;

		IMatrix techMatrix = data.techMatrix;
		TechIndex productIndex = data.techIndex;
		int idx = productIndex.getIndex(productIndex.getRefFlow());
		double[] s = solver.solve(techMatrix, idx, productIndex.getDemand());
		result.scalingVector = s;
		result.totalRequirements = getTotalRequirements(techMatrix, s);
		IMatrix enviMatrix = data.enviMatrix;

		result.totalFlowResults = solver.multiply(enviMatrix, s);

		if (data.impactMatrix != null) {
			addTotalImpacts(result);
		}

		if (data.costVector != null) {
			addTotalCosts(result, s);
		}

		return result;
	}

	public ContributionResult calculateContributions() {

		ContributionResult result = new ContributionResult();
		result.flowIndex = data.enviIndex;
		result.techIndex = data.techIndex;

		IMatrix techMatrix = data.techMatrix;
		TechIndex productIndex = data.techIndex;
		int idx = productIndex.getIndex(productIndex.getRefFlow());
		double[] s = solver.solve(techMatrix, idx, productIndex.getDemand());
		result.scalingVector = s;
		result.totalRequirements = getTotalRequirements(techMatrix, s);

		IMatrix enviMatrix = data.enviMatrix;
		IMatrix singleResult = enviMatrix.copy();
		solver.scaleColumns(singleResult, s);
		result.directFlowResults = singleResult;
		result.totalFlowResults = solver.multiply(enviMatrix, s);

		if (data.impactMatrix != null) {
			addTotalImpacts(result);
			addDirectImpacts(result);
		}

		if (data.costVector != null) {
			addTotalCosts(result, s);
			addDirectCosts(result, s);
		}
		return result;
	}

	public FullResult calculateFull() {

		FullResult result = new FullResult();
		result.flowIndex = data.enviIndex;
		result.techIndex = data.techIndex;

		TechIndex productIdx = data.techIndex;
		IMatrix techMatrix = data.techMatrix;
		IMatrix enviMatrix = data.enviMatrix;
		IMatrix inverse = solver.invert(techMatrix);
		double[] scalingVector = getScalingVector(inverse, productIdx);
		result.scalingVector = scalingVector;

		// direct results
		result.techMatrix = techMatrix.copy();
		solver.scaleColumns(result.techMatrix, scalingVector);
		result.directFlowResults = enviMatrix.copy();
		solver.scaleColumns(result.directFlowResults, scalingVector);
		result.totalRequirements = getTotalRequirements(techMatrix,
				scalingVector);

		// upstream results
		result.loopFactor = getLoopFactor(
				techMatrix, scalingVector, productIdx);
		double[] demands = getRealDemands(
				result.totalRequirements, result.loopFactor);
		IMatrix totalResult = solver.multiply(enviMatrix, inverse);
		if (data.costVector == null) {
			inverse = null; // allow GC
		}
		solver.scaleColumns(totalResult, demands);
		result.upstreamFlowResults = totalResult;
		int refIdx = productIdx.getIndex(productIdx.getRefFlow());
		result.totalFlowResults = totalResult.getColumn(refIdx);

		if (data.impactMatrix != null) {
			addDirectImpacts(result);
			IMatrix factors = data.impactMatrix;
			IMatrix totalImpactResult = solver.multiply(factors, totalResult);
			result.upstreamImpactResults = totalImpactResult;
			// total impacts = upstream result of reference product
			result.impactIndex = data.impactIndex;
			result.totalImpactResults = totalImpactResult.getColumn(refIdx);
		}

		if (data.costVector != null) {
			addDirectCosts(result, scalingVector);
			IMatrix costValues = CostVector.asMatrix(solver, data.costVector);
			IMatrix upstreamCosts = solver.multiply(costValues, inverse);
			solver.scaleColumns(upstreamCosts, demands);
			result.totalCosts = upstreamCosts.get(0, refIdx);
			result.upstreamCostResults = upstreamCosts;
		}

		return result;

	}

	/**
	 * Calculates the scaling vector for the reference product i from the given
	 * inverse of the technology matrix:
	 *
	 * s = d[i] .* Inverse[:, i]
	 *
	 * where d is the demand vector and.
	 *
	 */
	public double[] getScalingVector(IMatrix inverse, TechIndex techIndex) {
		ProcessProduct refProduct = techIndex.getRefFlow();
		int idx = techIndex.getIndex(refProduct);
		double[] s = inverse.getColumn(idx);
		double demand = techIndex.getDemand();
		for (int i = 0; i < s.length; i++)
			s[i] *= demand;
		return s;
	}

	/**
	 * Calculates the total requirements of the respective product amounts to
	 * fulfill the demand of the product system:
	 *
	 * tr = s .* diag(A)
	 *
	 * where s is the scaling vector and A the technology matrix.
	 *
	 */
	public double[] getTotalRequirements(IMatrix techMatrix,
			double[] scalingVector) {
		double[] tr = new double[scalingVector.length];
		for (int i = 0; i < scalingVector.length; i++) {
			tr[i] = scalingVector[i] * techMatrix.get(i, i);
		}
		return tr;
	}

	public static double getLoopFactor(
			IMatrix A, double[] s, TechIndex techIndex) {
		int i = techIndex.getIndex(techIndex.getRefFlow());
		double t = A.get(i, i) * s[i];
		double f = techIndex.getDemand();
		if (Math.abs(t - f) < 1e-12)
			return 1;
		return f / t;
	}

	/**
	 * Calculate the real demand vector for the analysis.
	 */
	public double[] getRealDemands(double[] totalRequirements,
			double loopFactor) {
		double[] rd = new double[totalRequirements.length];
		if (loopFactor != 1) {
			for (int k = 0; k < totalRequirements.length; k++)
				rd[k] = loopFactor * totalRequirements[k];
		} else {
			int length = totalRequirements.length;
			System.arraycopy(totalRequirements, 0, rd, 0, length);
		}
		return rd;
	}

	private void addTotalImpacts(SimpleResult result) {
		result.impactIndex = data.impactIndex;
		IMatrix factors = data.impactMatrix;
		double[] totals = solver.multiply(factors, result.totalFlowResults);
		result.totalImpactResults = totals;
	}

	private void addDirectImpacts(ContributionResult result) {
		IMatrix factors = data.impactMatrix;
		result.impactFactors = factors;
		IMatrix directResults = solver.multiply(factors,
				result.directFlowResults);
		result.directImpactResults = directResults;
		IMatrix singleFlowImpacts = factors.copy();
		solver.scaleColumns(singleFlowImpacts, result.totalFlowResults);
		result.directFlowImpacts = singleFlowImpacts;
	}

	private void addTotalCosts(SimpleResult result, double[] scalingVector) {
		double[] costValues = data.costVector;
		double total = 0;
		for (int i = 0; i < scalingVector.length; i++) {
			total += scalingVector[i] * costValues[i];
		}
		result.totalCosts = total;
	}

	private void addDirectCosts(ContributionResult result,
			double[] scalingVector) {
		double[] costValues = data.costVector;
		double[] directCosts = new double[costValues.length];
		for (int i = 0; i < scalingVector.length; i++) {
			directCosts[i] = costValues[i] * scalingVector[i];
		}
		result.directCostResults = directCosts;
	}

}
