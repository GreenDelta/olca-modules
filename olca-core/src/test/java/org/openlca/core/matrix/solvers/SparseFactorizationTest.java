package org.openlca.core.matrix.solvers;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.core.DataDir;
import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.nativelib.Module;
import org.openlca.nativelib.NativeLib;

public class SparseFactorizationTest {

	@BeforeClass
	public static void setUp() {
		NativeLib.loadFrom(DataDir.get().root());
	}

	@Before
	public void assumeLibsLoaded() {
		assumeTrue(NativeLib.isLoaded(Module.UMFPACK));
	}

	@Test
	public void testDispose() {
		var hashPoints = new HashPointMatrix(5, 5);
		for (int i = 0; i < 5; i++) {
			hashPoints.set(i, i, 1.0);
		}
		var f = SparseFactorization.of(CSCMatrix.of(hashPoints));
		f.dispose();
		assertTrue(f.isDisposed());
	}

	@Test
	public void testSingleSolution() {
		var m = HashPointMatrix.of(new double[][] {
				{ 2.0, 3.0, 0.0, 0.0, 0.0 },
				{ 3.0, 0.0, 4.0, 0.0, 6.0 },
				{ 0.0, -1.0, -3.0, 2.0, 0.0 },
				{ 0.0, 0.0, 1.0, 0.0, 0.0 },
				{ 0.0, 4.0, 2.0, 0.0, 1.0 } });
		var csc = CSCMatrix.of(m);
		var f = SparseFactorization.of(csc);
		double[] b = { 8., 45., -3., 3., 19. };
		double[] x = f.solve(b);
		assertArrayEquals(
				new double[] { 1d, 2d, 3d, 4d, 5d }, x, 1e-8);
		f.dispose();
		assertTrue(f.isDisposed());
	}

	@Test
	public void testIdentity() {
		var hashPoints = new HashPointMatrix(100, 100);
		for (int i = 0; i < 100; i++) {
			hashPoints.set(i, i, 1.0);
		}
		var csc = CSCMatrix.of(hashPoints);
		var f = SparseFactorization.of(csc);
		for (int i = 0; i < 100; i++) {
			var b = new double[100];
			b[i] = 1.0;
			var x = f.solve(b);
			assertArrayEquals(b, x, 1e-10);
		}
		f.dispose();
		assertTrue(f.isDisposed());
	}
}
