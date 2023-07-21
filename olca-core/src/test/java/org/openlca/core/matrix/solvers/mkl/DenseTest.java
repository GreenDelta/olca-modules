package org.openlca.core.matrix.solvers.mkl;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class DenseTest {

	@BeforeClass
	public static void setup() {
		Assume.assumeTrue(MKL.loadFromDefault());
	}

	@Test
	public void testMatrixMul() {
		double[] a = {1, 4, 2, 5, 3, 6};
		double[] b = {7, 8, 9, 10, 11, 12};
		double[] c = new double[4];
		MKL.denseMatrixMul(2, 2, 3, a, b, c);
		assertArrayEquals(new double[]{50, 122, 68, 167}, c, 1e-16);
	}

	@Test
	public void testMatrixVectorMul() {
		double[] a = {1, 4, 2, 5, 3, 6};
		double[] x = {2, 1, 0.5};
		double[] y = new double[2];
		MKL.denseMatrixVectorMul(2, 3, a, x, y);
		assertArrayEquals(new double[]{5.5, 16}, y, 1e-16);
	}

	@Test
	public void testSolve() {
		double[] a = {1, -4, 0, 2};
		double[] b = {1, 0, 0, 1};
		int info = MKL.solveDense(2, 2, a, b);
		assertEquals(0, info);
		assertArrayEquals(new double[]{1, 2, 0, 0.5}, b, 1e-16);
	}

	@Test
	public void testSolveSingularMatrix() {
		double[] a = {1, -1, 0, 0};
		double[] b = {1, 0};
		int info = MKL.solveDense(2, 1, a, b);
		assertTrue(info > 0); // info > 0 indicates that A was singular
	}

	@Test
	public void testInvert() {
		double[] a = {1, -4, 0, 2};
		MKL.invertDense(2, a);
		assertArrayEquals(new double[]{1, 2, 0, 0.5}, a, 1e-16);
	}

	@Test
	public void testInvertSingularMatrix() {
		double[] a = {1, -1, 0, 0};
		int info = MKL.invertDense(2, a);
		assertTrue(info > 0); // info > 0 indicates that A was singular
	}
}
