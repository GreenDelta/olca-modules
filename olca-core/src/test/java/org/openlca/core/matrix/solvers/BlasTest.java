package org.openlca.core.matrix.solvers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.core.DataDir;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.julia.Julia;
import org.openlca.nativelib.NativeLib;

public class BlasTest {

	@BeforeClass
	public static void setup() {
		NativeLib.loadFrom(DataDir.get().root());
	}

	@Before
	public void assumeLibsLoaded() {
		assumeTrue(NativeLib.isLoaded());
	}

	@Test
	public void testMatrixMatrixMult() {
		double[] a = { 1, 4, 2, 5, 3, 6 };
		double[] b = { 7, 8, 9, 10, 11, 12 };
		double[] c = new double[4];
		Julia.mmult(2, 2, 3, a, b, c);
		Assert.assertArrayEquals(new double[] { 50, 122, 68, 167 }, c, 1e-16);
	}

	@Test
	public void testSparseMatrixMatrixMult() {
		// currently auto-conversion to a dense matrix
		var a = HashPointMatrix.of(new double[][] {
			{ 1, 2, 3 },
			{ 4, 5, 6 }
		});
		var b = HashPointMatrix.of(new double[][] {
			{ 7, 10 },
			{ 8, 11 },
			{ 9, 12 }
		});
		var solver = new NativeSolver();
		DenseMatrix m = solver.multiply(a, b);
		assertEquals(m.get(0, 0), 50, 1e-10);
	}

	@Test
	public void testMatrixVectorMult() {
		double[] a = { 1, 4, 2, 5, 3, 6 };
		double[] x = { 2, 1, 0.5 };
		double[] y = new double[2];
		Julia.mvmult(2, 3, a, x, y);
		Assert.assertArrayEquals(new double[] { 5.5, 16 }, y, 1e-16);
	}
}
