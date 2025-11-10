package org.openlca.core.matrix.solvers.mkl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;

public class FactorizationTest {

	@BeforeClass
	public static void setup() {
		Assume.assumeTrue(MKL.loadFromDefault());
	}

	@Test
	public void testSparse() {
		var m = HashPointMatrix.of(new double[][] {
			{1, -0.5},
			{-1, 1.0}
		});
		var f = new MKLSolver().factorize(m);
		assertTrue(f instanceof SparseFactorization);
		var x = f.solve(new double[] {1.0, 0.0});
		f.dispose();
		assertEquals(2.0, x[0], 1e-16);
		assertEquals(2.0, x[1], 1e-16);
	}

	@Test
	public void testDense() {
		var m = DenseMatrix.of(new double[][] {
			{1, -0.5},
			{-1, 1.0}
		});
		var f = new MKLSolver().factorize(m);
		assertTrue(f instanceof DenseFactorization);
		var x = f.solve(new double[] {1.0, 0.0});
		f.dispose();
		assertEquals(2.0, x[0], 1e-16);
		assertEquals(2.0, x[1], 1e-16);
	}
}
