package org.openlca.core.results.solutions;

import org.openlca.core.matrix.DIndex;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public class EagerSolutionProvider implements SolutionProvider {

	private final MatrixData data;

	private final IMatrix inverse;
	private final double[] scalingVector;
	private final double[] totalRequirements;

	private double[] totalFlows;
	private IMatrix directFlows;
	private IMatrix totalFlowsOfOne;

	private double[] totalImpacts;
	private IMatrix directImpacts;
	private IMatrix totalImpactsOfOne;

	private double totalCosts;
	private double[] directCosts;
	private double[] totalCostsOfOne;

	private EagerSolutionProvider(MatrixData data, IMatrixSolver solver) {
		this.data = data;

		// product and waste flows
		var techIdx = data.techIndex;
		inverse = solver.invert(data.techMatrix);
		var refIdx = techIdx.getIndex(techIdx.getRefFlow());
		scalingVector = inverse.getColumn(refIdx);
		var demand = techIdx.getDemand();
		for (int i = 0; i < scalingVector.length; i++) {
			scalingVector[i] *= demand;
		}
		totalRequirements = data.techMatrix.diag();
		for (int i = 0; i < totalRequirements.length; i++) {
			totalRequirements[i] *= scalingVector[i];
		}

		if (data.flowMatrix != null) {

			// elementary flows
			directFlows = data.flowMatrix.copy();
			directFlows.scaleColumns(scalingVector);

			// the intensity matrix: M = B * inv(A)
			totalFlowsOfOne = solver.multiply(data.flowMatrix, inverse);
			totalFlows = totalFlowResultsOfOne(refIdx);
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

	public static EagerSolutionProvider create(
			MatrixData data,
			IMatrixSolver solver) {
		return new EagerSolutionProvider(data, solver);
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
	public DIndex<ImpactCategoryDescriptor> impactIndex() {
		return data.impactIndex;
	}

	@Override
	public double[] scalingVector() {
		return scalingVector;
	}

	@Override
	public double[] totalRequirements() {
		return totalRequirements;
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
	public double[] flowColumnOf(int j) {
		return data.flowMatrix == null
				? new double[0]
				: data.flowMatrix.getColumn(j);
	}

	@Override
	public double flowValueOf(int flow, int product) {
		return data.flowMatrix == null
				? 0
				: data.flowMatrix.get(flow, product);
	}

	@Override
	public double[] directFlowResultsOf(int product) {
		if (directFlows == null)
			return new double[0];
		return directFlows.getColumn(product);
	}

	@Override
	public double directFlowResultOf(int flow, int product) {
		if (directFlows == null)
			return 0;
		return directFlows.get(flow, product);
	}

	@Override
	public double[] totalFlowResults() {
		return totalFlows == null
				? new double[0]
				: totalFlows;
	}

	@Override
	public double[] totalFlowResultsOfOne(int product) {
		if (totalFlowsOfOne == null)
			return new double[0];
		return totalFlowsOfOne.getColumn(product);
	}

	@Override
	public double totalFlowResultOfOne(int flow, int product) {
		if (totalFlowsOfOne == null)
			return 0;
		return totalFlowsOfOne.get(flow, product);
	}

	@Override
	public double[] totalImpacts() {
		return totalImpacts == null
				? new double[0]
				: totalImpacts;
	}

	@Override
	public double[] totalImpactsOfOne(int product) {
		if (totalImpactsOfOne == null)
			return new double[0];
		return totalImpactsOfOne.getColumn(product);
	}

	@Override
	public double totalImpactOfOne(int indicator, int product) {
		if (totalImpactsOfOne == null)
			return 0;
		return totalImpactsOfOne.get(indicator, product);
	}

	@Override
	public double totalCosts() {
		return totalCosts;
	}

	@Override
	public double totalCostsOfOne(int product) {
		if (totalCostsOfOne == null)
			return 0;
		return totalCostsOfOne[product];
	}

	@Override
	public double loopFactorOf(int i) {
		var aii = data.techMatrix.get(i, i);
		var ii = inverse.get(i, i);
		var f = aii * ii;
		return f == 0
				? 0
				: 1 / f;
	}
}
