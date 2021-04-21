package org.openlca.core.results.providers;

import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.ImpactIndex;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.solvers.MatrixSolver;

public class EagerResultProvider implements ResultProvider {

	private final MatrixData data;

	private final Matrix inverse;
	private final double[] scalingVector;
	private final double[] totalRequirements;
	private final double[] loopFactors;

	private double[] totalFlows;
	private Matrix directFlows;
	private Matrix totalFlowsOfOne;

	private double[] totalImpacts;
	private Matrix directImpacts;
	private Matrix totalImpactsOfOne;

	private double totalCosts;
	private double[] directCosts;
	private double[] totalCostsOfOne;

	private EagerResultProvider(MatrixData data) {
		this.data = data;

		// product and waste flows
		var techIdx = data.techIndex;
		var solver = MatrixSolver.Instance.getNew();
		inverse = solver.invert(data.techMatrix);
		var refIdx = techIdx.of(techIdx.getRefFlow());
		scalingVector = inverse.getColumn(refIdx);
		var demand = techIdx.getDemand();
		for (int i = 0; i < scalingVector.length; i++) {
			scalingVector[i] *= demand;
		}
		totalRequirements = data.techMatrix.diag();
		for (int i = 0; i < totalRequirements.length; i++) {
			totalRequirements[i] *= scalingVector[i];
		}
		loopFactors = new double[techIdx.size()];
		for (int i = 0; i < loopFactors.length; i++) {
			var aii = data.techMatrix.get(i, i);
			var ii = inverse.get(i, i);
			var f = aii * ii;
			loopFactors[i] = f == 0
					? 1.0
					: 1 / f;
		}

		if (data.flowMatrix != null) {

			// elementary flows
			directFlows = data.flowMatrix.asMutableCopy();
			directFlows.scaleColumns(scalingVector);

			// the intensity matrix: M = B * inv(A)
			totalFlowsOfOne = solver.multiply(data.flowMatrix, inverse);
			totalFlows = totalFlowsOfOne(refIdx);
			for (int i = 0; i < totalFlows.length; i++) {
				totalFlows[i] *= demand;
			}

			if (data.impactMatrix != null) {

				directImpacts = solver.multiply(
						data.impactMatrix,
						directFlows);
				totalImpactsOfOne = solver.multiply(
						data.impactMatrix,
						totalFlowsOfOne);
				totalImpacts = totalImpactsOfOne(refIdx);
				for (int i = 0; i < totalImpacts.length; i++) {
					totalImpacts[i] *= demand;
				}
			}
		}

		// costs
		if (data.costVector != null) {
			var n = data.costVector.length;
			directCosts = new double[n];
			var costMatrix = solver.matrix(1, n);
			for (int j = 0; j < n; j++) {
				var costs = data.costVector[j];
				directCosts[j] = costs * scalingVector[j];
				costMatrix.set(0, j, costs);
			}
			totalCostsOfOne = solver.multiply(costMatrix, inverse).getRow(0);
			totalCosts = totalCostsOfOne(refIdx) * demand;
		}
	}

	public static EagerResultProvider create(MatrixData data) {
		return new EagerResultProvider(data);
	}

	@Override
	public TechIndex techIndex() {
		return data.techIndex;
	}

	@Override
	public FlowIndex flowIndex() {
		return data.flowIndex;
	}

	@Override
	public ImpactIndex impactIndex() {
		return data.impactIndex;
	}

	@Override
	public boolean hasCosts() {
		return !isEmpty(data.costVector);
	}

	@Override
	public double[] scalingVector() {
		return scalingVector;
	}

	@Override
	public double scalingFactorOf(int product) {
		return scalingVector[product];
	}

	@Override
	public double[] totalRequirements() {
		return totalRequirements;
	}

	@Override
	public double totalRequirementsOf(int product) {
		return totalRequirements[product];
	}

	@Override
	public double[] techColumnOf(int j) {
		return data.techMatrix.getColumn(j);
	}

	@Override
	public double techValueOf(int row, int col) {
		return data.techMatrix.get(row, col);
	}

	@Override
	public double[] solutionOfOne(int product) {
		return inverse.getColumn(product);
	}

	@Override
	public double loopFactorOf(int product) {
		return loopFactors[product];
	}

	@Override
	public double[] unscaledFlowsOf(int j) {
		return data.flowMatrix == null
				? EMPTY_VECTOR
				: data.flowMatrix.getColumn(j);
	}

	@Override
	public double unscaledFlowOf(int flow, int product) {
		return data.flowMatrix == null
				? 0
				: data.flowMatrix.get(flow, product);
	}

	@Override
	public double[] directFlowsOf(int product) {
		return directFlows == null
				? EMPTY_VECTOR
				: directFlows.getColumn(product);
	}

	@Override
	public double directFlowOf(int flow, int product) {
		return directFlows == null
				? 0
				: directFlows.get(flow, product);
	}

	@Override
	public double[] totalFlowsOfOne(int product) {
		return totalFlowsOfOne == null
				? EMPTY_VECTOR
				: totalFlowsOfOne.getColumn(product);
	}

	@Override
	public double totalFlowOfOne(int flow, int product) {
		return totalFlowsOfOne == null
				? 0
				: totalFlowsOfOne.get(flow, product);
	}

	@Override
	public double[] totalFlowsOf(int product) {
		var factor = totalFactorOf(product);
		var totals = totalFlowsOfOne(product);
		scaleInPlace(totals, factor);
		return totals;
	}

	@Override
	public double[] totalFlows() {
		return totalFlows == null
				? EMPTY_VECTOR
				: totalFlows;
	}

	@Override
	public double[] impactFactorsOf(int flow) {
		return data.impactMatrix == null
				? EMPTY_VECTOR
				: data.impactMatrix.getColumn(flow);
	}

	@Override
	public double impactFactorOf(int indicator, int flow) {
		return data.impactMatrix == null
				? 0
				: data.impactMatrix.get(indicator, flow);
	}

	@Override
	public double[] flowImpactsOf(int flow) {
		if (totalFlows == null)
			return EMPTY_VECTOR;
		var total = totalFlows[flow];
		var impacts = impactFactorsOf(flow);
		scaleInPlace(impacts, total);
		return impacts;
	}

	@Override
	public double flowImpactOf(int indicator, int flow) {
		if (totalFlows == null)
			return 0;
		var total = totalFlows[flow];
		return total * impactFactorOf(indicator, flow);
	}

	@Override
	public double[] directImpactsOf(int product) {
		return directImpacts == null
				? EMPTY_VECTOR
				: directImpacts.getColumn(product);
	}

	@Override
	public double directImpactOf(int indicator, int product) {
		return directImpacts == null
				? 0
				: directImpacts.get(indicator, product);
	}


	@Override
	public double[] totalImpactsOfOne(int product) {
		return totalImpactsOfOne == null
				? EMPTY_VECTOR
				: totalImpactsOfOne.getColumn(product);
	}

	@Override
	public double totalImpactOfOne(int indicator, int product) {
		return totalImpactsOfOne == null
				? 0
				: totalImpactsOfOne.get(indicator, product);
	}

	@Override
	public double[] totalImpacts() {
		return totalImpacts == null
				? EMPTY_VECTOR
				: totalImpacts;
	}

	@Override
	public double directCostsOf(int product) {
		return directCosts == null
				? 0
				: directCosts[product];
	}

	@Override
	public double totalCostsOfOne(int product) {
		return totalCostsOfOne == null
				? 0
				: totalCostsOfOne[product];
	}

	@Override
	public double totalCosts() {
		return totalCosts;
	}
}
