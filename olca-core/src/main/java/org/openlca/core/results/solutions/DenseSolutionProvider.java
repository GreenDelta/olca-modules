package org.openlca.core.results.solutions;

import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;

public class DenseSolutionProvider implements SolutionProvider {

	private double[] scalingVector;
	private IMatrix techMatrix;
	private IMatrix inverse;
	private IMatrix intensities;
	private IMatrix impacts;
	private double[] costs;

	private DenseSolutionProvider() {
	}

	public static DenseSolutionProvider create(
			MatrixData data,
			IMatrixSolver solver) {
		var provider = new DenseSolutionProvider();
		provider.techMatrix = data.techMatrix;

		// the inverse of A: inv(A)
		provider.inverse = solver.invert(data.techMatrix);

		// the scaling vector s = A \ d
		var refIdx = data.techIndex.getIndex(
				data.techIndex.getRefFlow());
		provider.scalingVector = provider.inverse.getColumn(refIdx);
		var d = data.techIndex.getDemand();
		for (int i = 0; i < provider.scalingVector.length; i++) {
			provider.scalingVector[i] *= d;
		}

		if (data.enviMatrix != null) {

			// the intensity matrix: M = B * inv(A)
			provider.intensities = solver.multiply(
					data.enviMatrix,
					provider.inverse);

			if (data.impactMatrix != null) {

				// impacts of the intensities: C * M
				provider.impacts = solver.multiply(
						data.impactMatrix,
						provider.intensities);
			}
		}

		if (data.costVector != null) {
			var n = data.costVector.length;
			var costs = solver.matrix(1, n);
			for (int col = 0; col < n; col++) {
				costs.set(0, col, data.costVector[col]);
			}
			provider.costs = solver.multiply(
					costs, provider.inverse)
					.getRow(0);
		}

		return provider;
	}

	@Override
	public double[] scalingVector() {
		return scalingVector;
	}

	@Override
	public double[] directRequirements(int product) {
		return techMatrix.getColumn(product);
	}

	@Override
	public double[] solution(int product) {
		return inverse.getColumn(product);
	}

	@Override
	public boolean hasIntensities() {
		return intensities != null;
	}

	@Override
	public double[] intensities(int product) {
		if (intensities == null)
			return new double[0];
		return intensities.getColumn(product);
	}

	@Override
	public double intensity(int flow, int product) {
		if (intensities == null)
			return 0;
		return intensities.get(flow, product);
	}

	@Override
	public boolean hasImpacts() {
		return impacts != null;
	}

	@Override
	public double[] impacts(int product) {
		if (impacts == null)
			return new double[0];
		return impacts.getColumn(product);
	}

	@Override
	public double impact(int indicator, int product) {
		if (impacts == null)
			return 0;
		return impacts.get(indicator, product);
	}

	@Override
	public boolean hasCosts() {
		return costs != null;
	}

	@Override
	public double costs(int product) {
		if (costs == null)
			return 0;
		return costs[product];
	}

	@Override
	public double getLoopFactor(int i) {
		var aii = techMatrix.get(i, i);
		var ii = inverse.get(i, i);
		var f = aii * ii;
		return f == 0
				? 0
				: 1 / f;
	}
}
