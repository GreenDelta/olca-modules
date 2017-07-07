package org.openlca.core.matrix.solvers;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openlca.core.matrix.format.IMatrix;

public class PrecisionBenchmarkTest {

	static {
		TestSession.loadLib();
	}

	private final int MAX_SIZE = 4000;
	private final int STEP = 500;
	private final float FACTOR = 1e-6f;
	private final double epsilon = 1e-4;
	private final boolean WITH_LOOPS = true;
	private final boolean CHECK_ASSERTS = false;

	@Test
	@Ignore
	public void testDoublePrecision() {
		System.out.println("\nRun benchmark for double precision");
		System.out.println("Matrix size \t Time (ms) \t DiagSum \t Success");
		for (int i = STEP; i <= MAX_SIZE; i += STEP) {
			runAndLog(i, new DenseSolver());
		}
	}

	private void runAndLog(int i, IMatrixSolver solver) {
		long start = System.currentTimeMillis();
		double s = runCalculation(i, solver);
		long end = System.currentTimeMillis();
		boolean success = Math.abs(s - i) < epsilon;
		System.out
				.printf("%s \t %s \t %s \t %s \n", i, end - start, s, success);
		if (CHECK_ASSERTS)
			Assert.assertTrue(success);
	}

	private double runCalculation(int i, IMatrixSolver solver) {
		IMatrix matrix = solver.matrix(i, i);
		for (int row = 0; row < i; row++) {
			for (int col = 0; col < i; col++) {
				if (row == col)
					matrix.set(row, col, 1000 * Math.random());
				else if (col < row)
					matrix.set(row, col, -1 * Math.random() * FACTOR);
				else if (WITH_LOOPS)
					matrix.set(row, col, -1 * Math.random() * FACTOR
							* FACTOR);
			}
		}
		// BlockInversion blockInversion = new BlockInversion(2000,
		// TestSession.getMatrixFactory());
		// IMatrix inverse = blockInversion.run(matrix);
		IMatrix inverse = solver.invert(matrix);
		IMatrix eye = solver.multiply(matrix, inverse);
		double s = 0;
		for (int row = 0; row < i; row++) {
			for (int col = 0; col < i; col++) {
				s += Math.abs(eye.get(row, col));
			}
		}
		return s;
	}

}
