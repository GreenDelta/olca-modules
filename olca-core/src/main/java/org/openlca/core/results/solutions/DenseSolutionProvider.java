package org.openlca.core.results.solutions;

import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;

public class DenseSolutionProvider implements SolutionProvider {

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
	public double[] solution(int i) {
		return inverse.getColumn(i);
	}

	@Override
	public boolean hasIntensities() {
		return intensities != null;
	}

	@Override
	public double[] intensities(int i) {
		if (intensities == null)
			return new double[0];
		return intensities.getColumn(i);
	}

	@Override
	public boolean hasImpacts() {
		return impacts != null;
	}

	@Override
	public double[] impacts(int i) {
		if (impacts == null)
			return new double[0];
		return impacts.getColumn(i);
	}

	@Override
	public boolean hasCosts() {
		return costs != null;
	}

	@Override
	public double costs(int i) {
		if (costs == null)
			return 0;
		return costs[i];
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
