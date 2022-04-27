package org.openlca.core.matrix.solvers;

import static org.junit.Assume.assumeTrue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.core.DataDir;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.nativelib.NativeLib;

public class DenseFactorizationTest {

	private DenseFactorization factorization;

	@BeforeClass
	public static void setup() {
		NativeLib.loadFrom(DataDir.get().root());
	}

	@Before
	public void assumeLibsLoaded() {
		assumeTrue(NativeLib.isLoaded());
	}

	@Before
	public void factorize() {
		var matrix = new DenseMatrix(3, 3);
		matrix.setValues(new double[][] {
				{ 0.5, -5.0, 0.0 },
				{ -0.1, 2.0, -1.0 },
				{ 0.0, 0.0, 4.0 },
		});
		factorization = DenseFactorization.of(matrix);
	}

	@After
	public void tearDown() {
		factorization.dispose();
	}

	@Test
	public void testSolve() {
		var x = factorization.solve(2, 1.0);
		Assert.assertArrayEquals(
				new double[] { 2.5, 0.25, 0.25 }, x, 1e-10);
	}

	@Test
	public void testSolveVector() {
		var b = new double[3];
		b[2] = 1.0;
		var x = factorization.solve(b);
		Assert.assertArrayEquals(
				new double[] { 2.5, 0.25, 0.25 }, x, 1e-10);
	}

	@Test
	public void testSolveMatrix() {
		var i = new DenseMatrix(3, 3);
		for (int k = 0; k < 3; k++) {
			i.set(k, k, 1.0);
		}
		var inverse = factorization.solve(i);
		Assert.assertArrayEquals(
				new double[] { 2.5, 0.25, 0.25 },
				inverse.getColumn(2),
				1e-10);
	}
}
