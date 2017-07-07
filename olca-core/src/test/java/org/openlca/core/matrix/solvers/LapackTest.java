package org.openlca.core.matrix.solvers;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.eigen.Lapack;

public class LapackTest {

	static {
		TestSession.loadLib();
	}

	@Test
	public void testInvert() {
		double[] a = { 1, -4, 0, 2 };
		Lapack.dInvert(2, a);
		Assert.assertArrayEquals(new double[] { 1, 2, 0, 0.5 }, a, 1e-16);
	}

	@Test
	public void testInvertSingle() {
		float[] a = { 1, -4, 0, 2 };
		Lapack.sInvert(2, a);
		Assert.assertArrayEquals(new float[] { 1, 2, 0, 0.5f }, a, 1e-16f);
	}

	@Test
	public void testSolve() {
		double[] a = { 1, -4, 0, 2 };
		double[] b = { 1, 0, 0, 1 };
		int info = Lapack.dSolve(2, 2, a, b);
		Assert.assertTrue(info == 0);
		Assert.assertArrayEquals(new double[] { 1, 2, 0, 0.5 }, b, 1e-16);
	}

	@Test
	public void testSolveSingle() {
		float[] a = { 1, -4, 0, 2 };
		float[] b = { 1, 0, 0, 1 };
		int info = Lapack.sSolve(2, 2, a, b);
		Assert.assertTrue(info == 0);
		Assert.assertArrayEquals(new float[] { 1, 2, 0, 0.5f }, b, 1e-16f);
	}

	@Test
	public void testSolveIterRef() {
		double[] a = { 1, -4, 0, 2 };
		double[] b = { 1, 0, 0, 1 };
		double[] x = new double[4];
		Lapack.dsSolve(2, 2, a, b, x);
		Assert.assertArrayEquals(new double[] { 1, 2, 0, 0.5 }, x, 1e-16);
	}

	@Test
	public void testEquilibrate() {
		double[] a = { 1, -5, 0, 4 };
		double[] r = new double[2];
		double[] c = new double[2];
		Lapack.dEquilibrate(2, 2, a, r, c);
		double[] expectedR = { 1.0, 0.2 };
		double[] expectedC = { 1.0, 1.25 };
		Assert.assertArrayEquals(r, expectedR, 1e-16);
		Assert.assertArrayEquals(c, expectedC, 1e-16);
	}

	@Test
	public void testEquilibrateSingle() {
		float[] a = { 1, -5, 0, 4 };
		float[] r = new float[2];
		float[] c = new float[2];
		Lapack.sEquilibrate(2, 2, a, r, c);
		float[] expectedR = { 1.0f, 0.2f };
		float[] expectedC = { 1.0f, 1.25f };
		Assert.assertArrayEquals(r, expectedR, 1e-16f);
		Assert.assertArrayEquals(c, expectedC, 1e-16f);
	}
}
