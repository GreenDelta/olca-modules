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

		ProductIndex productIndex = inventory.getProductIndex();
		IMatrix techMatrix = inventory.getTechnologyMatrix();
		IMatrix enviMatrix = inventory.getInterventionMatrix();
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
		if (costVector == null)
			inverse = null; // allow GC
		solver.scaleColumns(totalResult, demands);
		result.upstreamFlowResults = totalResult;
		int refIdx = productIndex.getIndex(productIndex.getRefProduct());
		result.totalFlowResults = totalResult.getColumn(refIdx);
		result.linkContributions = LinkContributions.calculate(
				techMatrix, productIndex, scalingVector);

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

	private double[] getScalingVector(IMatrix inverse, ProductIndex productIndex) {
		LongPair refProduct = productIndex.getRefProduct();
		int idx = productIndex.getIndex(refProduct);
		double[] s = inverse.getColumn(idx);
		double demand = productIndex.getDemand();
		for (int i = 0; i < s.length; i++)
			s[i] *= demand;
		return s;
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
			total += scalingVector[i] + costValues[i];
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
