package org.openlca.core.matrix.solvers.mkl;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class SparseTest {

	@BeforeClass
	public static void setup() {
		Assume.assumeTrue(MKL.loadFromDefault());
	}

	@Test
	public void testSolveNative() {
		double[] x = new double[5];
		MKL.solveSparse(
			5,
			new double[]{2., 3., 3., -1., 4., 4., -3., 1., 2., 2., 6., 1.},
			new int[]{0, 1, 0, 2, 4, 1, 2, 3, 4, 2, 1, 4},
			new int[]{0, 2, 5, 9, 10, 12},
			new double[]{8., 45., -3., 3., 19.},
			x
		);
		assertArrayEquals(
			new double[]{1d, 2d, 3d, 4d, 5d}, x, 1e-8);
	}
}
