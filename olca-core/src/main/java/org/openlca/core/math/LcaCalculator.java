package org.openlca.core.math;

import org.openlca.core.matrix.CostVector;
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
	private final InventoryMatrix inventory;

	private ImpactMatrix impactMatrix;
	private CostVector costVector;

	public LcaCalculator(IMatrixSolver solver, InventoryMatrix inventory) {
		this.solver = solver;
		this.inventory = inventory;
	}

	public void setImpactMatrix(ImpactMatrix impactMatrix) {
		this.impactMatrix = impactMatrix;
	}

	public void setCostVector(CostVector costVector) {
		this.costVector = costVector;
	}

	public SimpleResult calculateSimple() {

		SimpleResult result = new SimpleResult();
		result.flowIndex = inventory.getFlowIndex();
		result.productIndex = inventory.getProductIndex();

		IMatrix techMatrix = inventory.getTechnologyMatrix();
		ProductIndex productIndex = inventory.getProductIndex();
		int idx = productIndex.getIndex(productIndex.getRefProduct());
		double[] s = solver.solve(techMatrix, idx, productIndex.getDemand());
		IMatrix enviMatrix = inventory.getInterventionMatrix();

		result.totalFlowResults = solver.multiply(enviMatrix, s);

		if (impactMatrix != null) {
			addTotalImpacts(result);
		}

		if (costVector != null) {
			result.hasCostResults = true;
			addTotalCosts(result, s);
		}

		return result;
	}

	public ContributionResult calculateContributions() {

		ContributionResult result = new ContributionResult();
		result.flowIndex = inventory.getFlowIndex();
		result.productIndex = inventory.getProductIndex();

		IMatrix techMatrix = inventory.getTechnologyMatrix();
		ProductIndex productIndex = inventory.getProductIndex();
		int idx = productIndex.getIndex(productIndex.getRefProduct());
		double[] s = solver.solve(techMatrix, idx, productIndex.getDemand());
		result.scalingFactors = s;
		result.totalRequirements = getTotalRequirements(techMatrix, s);

		IMatrix enviMatrix = inventory.getInterventionMatrix();
		IMatrix singleResult = enviMatrix.copy();
		solver.scaleColumns(singleResult, s);
		result.singleFlowResults = singleResult;
		result.totalFlowResults = solver.multiply(enviMatrix, s);
		result.linkContributions = LinkContributions.calculate(
				techMatrix, productIndex, s);

		if (impactMatrix != null) {
			addTotalImpacts(result);
			addDirectImpacts(result);
		}

		if (costVector != null) {
			result.hasCostResults = true;
			addTotalCosts(result, s);
			addDirectCosts(result, s);
		}
		return result;
	}

	public FullResult calculateFull() {

		FullResult result = new FullResult();
		result.flowIndex = inventory.getFlowIndex();
		result.productIndex = inventory.getProductIndex();

		ProductIndex productIdx = inventory.getProductIndex();
		IMatrix techMatrix = inventory.getTechnologyMatrix();
		IMatrix enviMatrix = inventory.getInterventionMatrix();
		IMatrix inverse = solver.invert(techMatrix);
		double[] scalingVector = getScalingVector(inverse, productIdx);
		result.scalingFactors = scalingVector;

		// direct results
		IMatrix singleResult = enviMatrix.copy();
		solver.scaleColumns(singleResult, scalingVector);
		result.singleFlowResults = singleResult;
		result.totalRequirements = getTotalRequirements(techMatrix, scalingVector);

		// upstream results
		double[] demands = getRealDemands(result.totalRequirements, productIdx);
		IMatrix totalResult = solver.multiply(enviMatrix, inverse);
		if (costVector == null)
			inverse = null; // allow GC
		solver.scaleColumns(totalResult, demands);
		result.upstreamFlowResults = totalResult;
		int refIdx = productIdx.getIndex(productIdx.getRefProduct());
		result.totalFlowResults = totalResult.getColumn(refIdx);
		result.linkContributions = LinkContributions.calculate(
				techMatrix, productIdx, scalingVector);

		if (impactMatrix != null) {
			addDirectImpacts(result);
			IMatrix factors = impactMatrix.getFactorMatrix();
			IMatrix totalImpactResult = solver.multiply(factors, totalResult);
			result.upstreamImpactResults = totalImpactResult;
			// total impacts = upstream result of reference product
			result.impactIndex = impactMatrix.getCategoryIndex();
			result.totalImpactResults = totalImpactResult.getColumn(refIdx);
		}

		if (costVector != null) {
			result.hasCostResults = true;
			addDirectCosts(result, scalingVector);
			IMatrix costValues = costVector.asMatrix(solver.getMatrixFactory());
			IMatrix upstreamCosts = solver.multiply(costValues, inverse);
			solver.scaleColumns(upstreamCosts, demands);
			result.totalCostResult = upstreamCosts.getEntry(0, refIdx);
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
	private double[] getScalingVector(IMatrix inverse, ProductIndex productIdx) {
		LongPair refProduct = productIdx.getRefProduct();
		int idx = productIdx.getIndex(refProduct);
		double[] s = inverse.getColumn(idx);
		double demand = productIdx.getDemand();
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
	private double[] getTotalRequirements(IMatrix techMatrix,
			double[] scalingVector) {
		double[] tr = new double[scalingVector.length];
		for (int i = 0; i < scalingVector.length; i++) {
			tr[i] = scalingVector[i] * techMatrix.getEntry(i, i);
		}
		return tr;
	}

	/**
	 * Calculate the real demand vector for the analysis.
	 */
	private double[] getRealDemands(double[] totalRequirements,
			ProductIndex productIdx) {
		double refDemand = productIdx.getDemand();
		int i = productIdx.getIndex(productIdx.getRefProduct());
		double[] rd = new double[totalRequirements.length];
		if (Math.abs(totalRequirements[i] - refDemand) > 1e-9) {
			// 'self-loop' correction for total result scale
			double f = refDemand / totalRequirements[i];
			for (int k = 0; k < totalRequirements.length; k++)
				rd[k] = f * totalRequirements[k];
		} else {
			int length = totalRequirements.length;
			System.arraycopy(totalRequirements, 0, rd, 0, length);
		}
		return rd;
	}

	private void addTotalImpacts(SimpleResult result) {
		result.impactIndex = impactMatrix.getCategoryIndex();
		IMatrix factors = impactMatrix.getFactorMatrix();
		double[] totals = solver.multiply(factors, result.totalFlowResults);
		result.totalImpactResults = totals;
	}

	private void addDirectImpacts(ContributionResult result) {
		IMatrix factors = impactMatrix.getFactorMatrix();
		result.impactFactors = factors;
		IMatrix directResults = solver.multiply(factors, result.singleFlowResults);
		result.singleImpactResults = directResults;
		IMatrix singleFlowImpacts = factors.copy();
		solver.scaleColumns(singleFlowImpacts, result.totalFlowResults);
		result.singleFlowImpacts = singleFlowImpacts;
	}

	private void addTotalCosts(SimpleResult result, double[] scalingVector) {
		double[] costValues = costVector.values;
		double total = 0;
		for (int i = 0; i < scalingVector.length; i++) {
			total += scalingVector[i] * costValues[i];
		}
		result.totalCostResult = total;
	}

	private void addDirectCosts(ContributionResult result, double[] scalingVector) {
		double[] costValues = costVector.values;
		double[] directCosts = new double[costValues.length];
		for (int i = 0; i < scalingVector.length; i++) {
			directCosts[i] = costValues[i] * scalingVector[i];
		}
		result.singleCostResults = directCosts;
	}

}
