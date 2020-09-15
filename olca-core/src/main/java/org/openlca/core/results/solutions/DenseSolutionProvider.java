package org.openlca.core.results.solutions;

import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;

public class DenseSolutionProvider implements SolutionProvider {

	private final MatrixData data;

	private double[] scalingVector;
	private double[] totalFlows;
	private double[] totalImpacts;
	private double totalCosts;

	private IMatrix inverse;
	private IMatrix flowIntensities;
	private IMatrix impactIntensities;
	private double[] costIntensities;

	private DenseSolutionProvider(MatrixData data) {
		this.data = data;
	}

	public static DenseSolutionProvider create(
			MatrixData data,
			IMatrixSolver solver) {
		var provider = new DenseSolutionProvider(data);

		// the inverse of A: inv(A)
		provider.inverse = solver.invert(data.techMatrix);

		// the scaling vector s = A \ d
		var refIdx = data.techIndex.getIndex(
				data.techIndex.getRefFlow());
		provider.scalingVector = provider.inverse.getColumn(refIdx);
		var demand = data.techIndex.getDemand();
		for (int i = 0; i < provider.scalingVector.length; i++) {
			provider.scalingVector[i] *= demand;
		}

		if (data.enviMatrix != null) {

			// the intensity matrix: M = B * inv(A)
			provider.flowIntensities = solver.multiply(
					data.enviMatrix,
					provider.inverse);
			provider.totalFlows = provider.totalFlowResultsOfOne(refIdx);
			for (int i = 0; i < provider.totalFlows.length; i++) {
				provider.totalFlows[i] *= demand;
			}

			if (data.impactMatrix != null) {

				// impacts of the intensities: C * M
				provider.impactIntensities = solver.multiply(
						data.impactMatrix,
						provider.flowIntensities);
				provider.totalImpacts = provider.totalImpactsOfOne(refIdx);
				for(int i = 0; i < provider.totalImpacts.length; i++) {
					provider.totalImpacts[i] *= demand;
				}
			}
		}

		if (data.costVector != null) {
			var n = data.costVector.length;
			var costs = solver.matrix(1, n);
			for (int col = 0; col < n; col++) {
				costs.set(0, col, data.costVector[col]);
			}
			provider.costIntensities = solver.multiply(
					costs, provider.inverse)
					.getRow(0);
			provider.totalCosts = provider.totalCostsOfOne(refIdx) * demand;
		}

		return provider;
	}

	@Override
	public TechIndex techIndex() {
		return data.techIndex;
	}

	@Override
	public double[] scalingVector() {
		return scalingVector;
	}

	@Override
	public double[] columnOfA(int j) {
		return data.techMatrix.getColumn(j);
	}

	@Override
	public double valueOfA(int row, int col) {
		return data.techMatrix.get(row, col);
	}

	@Override
	public double[] solutionOfOne(int product) {
		return inverse.getColumn(product);
	}

	@Override
	public boolean hasFlows() {
		return flowIntensities != null;
	}

	@Override
	public double[] totalFlowResults() {
		return totalFlows == null
				? new double[0]
				: totalFlows;
	}

	@Override
	public double[] totalFlowResultsOfOne(int product) {
		if (flowIntensities == null)
			return new double[0];
		return flowIntensities.getColumn(product);
	}

	@Override
	public double totalFlowResultOfOne(int flow, int product) {
		if (flowIntensities == null)
			return 0;
		return flowIntensities.get(flow, product);
	}

	@Override
	public boolean hasImpacts() {
		return impactIntensities != null;
	}

	@Override
	public double[] totalImpacts() {
		return totalImpacts == null
				? new double[0]
				: totalImpacts;
	}

	@Override
	public double[] totalImpactsOfOne(int product) {
		if (impactIntensities == null)
			return new double[0];
		return impactIntensities.getColumn(product);
	}

	@Override
	public double totalImpactOfOne(int indicator, int product) {
		if (impactIntensities == null)
			return 0;
		return impactIntensities.get(indicator, product);
	}

	@Override
	public boolean hasCosts() {
		return costIntensities != null;
	}

	@Override
	public double totalCosts() {
		return totalCosts;
	}

	@Override
	public double totalCostsOfOne(int product) {
		if (costIntensities == null)
			return 0;
		return costIntensities[product];
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
