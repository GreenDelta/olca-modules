package org.openlca.core.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.format.JavaMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;

/**
 * Tests the default methods of the ISolver interface.
 */
public class ISolverTest {

	@Test
	public void testScaleColumns() {
		Solver solver = new Solver();
		IMatrix m = solver.matrix(2, 3);
		double[][] vals = { { 1, 2, 3 }, { 4, 5, 6 } };
		m.setValues(vals);
		solver.scaleColumns(m, new double[] { 2, 1, 0.5 });
		double[][] expected = { { 2, 2, 1.5 }, { 8, 5, 3 } };
		for (int row = 0; row < 2; row++) {
			for (int col = 0; col < 3; col++) {
				assertEquals(expected[row][col], m.get(row, col), 1e-18);
			}
		}
	}

	@Test
	public void testMatrixVectorMultiplication() {
		Solver solver = new Solver();
		IMatrix m = solver.matrix(2, 3);
		m.setValues(new double[][] { { 1, 2, 3 }, { 4, 5, 6 } });
		double[] r = solver.multiply(m, new double[] { 2, 1, 0.5 });
		assertEquals(2, r.length);
		assertEquals(5.5, r[0], 1e-18);
		assertEquals(16, r[1], 1e-18);
	}

	@Test
	public void testMatrixMatrixMultiplication() {
		Solver solver = new Solver();
		IMatrix a = solver.matrix(2, 3);
		a.setValues(new double[][] { { 1, 2, 3 }, { 4, 5, 6 } });
		IMatrix b = solver.matrix(3, 3);
		b.setValues(new double[][] { { 2, 0, 0 }, { 0, 1, 0 }, { 0, 0, 0.5 } });
		IMatrix r = solver.multiply(a, b);
		assertEquals(2, r.rows());
		assertEquals(3, r.columns());
		double[][] expected = { { 2, 2, 1.5 }, { 8, 5, 3 } };
		for (int row = 0; row < 2; row++) {
			for (int col = 0; col < 3; col++) {
				assertEquals(expected[row][col], r.get(row, col), 1e-18);
			}
		}
	}

	private class Solver implements IMatrixSolver {

		@Override
		public IMatrix matrix(int rows, int columns) {
			return new JavaMatrix(rows, columns);
		}

		@Override
		public double[] solve(IMatrix a, int idx, double d) {
			return null;
		}

		@Override
		public IMatrix invert(IMatrix a) {
			return null;
		}

	}
}
