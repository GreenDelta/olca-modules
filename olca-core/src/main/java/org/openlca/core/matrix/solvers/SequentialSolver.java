package org.openlca.core.matrix.solvers;

import org.openlca.core.matrix.format.HashMatrix;
import org.openlca.core.matrix.format.IMatrix;

public class SequentialSolver implements IMatrixSolver {

	private final double cutoff;
	private final int maxIterations;

	private int breakIndex = -1;
	private int breakIteration = -1;

	private int[] iterations;

	public SequentialSolver(double cutoff, int maxIterations) {
		this.cutoff = Math.abs(cutoff);
		this.maxIterations = maxIterations;
	}

	/**
	 * Set maximum number of iteration for the given index.
	 */
	public void setBreak(int index, int iteration) {
		this.breakIndex = index;
		this.breakIteration = iteration;
	}

	@Override
	public double[] solve(IMatrix a, int index, double demand) {
		double[] s = new double[a.rows()];
		iterations = new int[s.length];
		next(a, index, demand, s);
		return s;
	}

	public int[] getIterations() {
		return iterations;
	}

	private void next(IMatrix a, int idx, double demand, double[] s) {
		int iteration = iterations[idx];
		if (iteration >= maxIterations)
			return;
		if (idx == breakIndex && iteration >= breakIteration)
			return;
		double factor = demand / a.get(idx, idx);
		if (Math.abs(factor) < cutoff)
			return;
		iterations[idx]++;
		s[idx] += factor;
		for (int row = 0; row < a.rows(); row++) {
			if (row == idx)
				continue;
			double val = a.get(row, idx);
			if (val == 0)
				continue;
			double nextDemand = factor * (-val);
			next(a, row, nextDemand, s);
		}
	}

	@Override
	public IMatrix invert(IMatrix a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMatrix matrix(int rows, int columns) {
		return new HashMatrix(rows, columns);
	}

}
