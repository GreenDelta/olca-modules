package org.openlca.core.matrix.solvers;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.eigen.Blas;

public class BlasTest {

	static {
		TestSession.loadLib();
	}

	@Test
	public void testMatrixMatrixMult() {
		double[] a = { 1, 4, 2, 5, 3, 6 };
		double[] b = { 7, 8, 9, 10, 11, 12 };
		double[] c = new double[4];
		Blas.dMmult(2, 2, 3, a, b, c);
		Assert.assertArrayEquals(new double[] { 50, 122, 68, 167 }, c, 1e-16);
	}

	@Test
	public void testMatrixMatrixMultSingle() {
		float[] a = { 1, 4, 2, 5, 3, 6 };
		float[] b = { 7, 8, 9, 10, 11, 12 };
		float[] c = new float[4];
		Blas.sMmult(2, 2, 3, a, b, c);
		Assert.assertArrayEquals(new float[] { 50, 122, 68, 167 }, c, 1e-16f);
	}

	@Test
	public void testMatrixVectorMult() {
		double[] a = { 1, 4, 2, 5, 3, 6 };
		double[] x = { 2, 1, 0.5 };
		double[] y = new double[2];
		Blas.dMVmult(2, 3, a, x, y);
		Assert.assertArrayEquals(new double[] { 5.5, 16 }, y, 1e-16);
	}

	@Test
	public void testMatrixVectorMultSingle() {
		float[] a = { 1, 4, 2, 5, 3, 6 };
		float[] x = { 2, 1, 0.5f };
		float[] y = new float[2];
		Blas.sMVmult(2, 3, a, x, y);
		Assert.assertArrayEquals(new float[] { 5.5f, 16 }, y, 1e-16f);
	}
}
