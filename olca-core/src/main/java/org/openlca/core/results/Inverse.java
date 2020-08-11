package org.openlca.core.results;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;

/**
 * The inverse of a technology matrix of a product system contains in each
 * column $j$ the total requirements of each product $i$ in order to produce
 * 1 unit of output of product $j$.
 */
public abstract class Inverse {

	public abstract double[] getColumn(int j);

	public static Inverse eager(FullResult result, IMatrixSolver solver) {
		var inv = solver.invert(result.techMatrix);
		return new DenseInverse(inv);
	}

	public static Inverse lazy(FullResult result, IMatrixSolver solver) {
		return new LazyInverse(result, solver);
	}

	private static class DenseInverse extends Inverse {

		final IMatrix inv;

		DenseInverse(IMatrix inv) {
			this.inv = inv;
		}

		@Override
		public double[] getColumn(int j) {
			return inv.getColumn(j);
		}
	}

	// TODO: we should store the factorized matrix
	private static class LazyInverse extends Inverse {

		private final IMatrix techMatrix;
		private final IMatrixSolver solver;
		private final TIntObjectHashMap<double[]> cache;

		LazyInverse(FullResult r, IMatrixSolver solver) {
			this.techMatrix = r.techMatrix;
			this.solver = solver;
			this.cache = new TIntObjectHashMap<>();
		}

		@Override
		public double[] getColumn(int j) {
			var cached = cache.get(j);
			if (cached != null)
				return cached;
			// TODO: for the ref. product we do not
			// need to solve it again; waste flows?
			var col = solver.solve(techMatrix, j, 1.0);
			cache.put(j, col);
			return col;
		}
	}



}
