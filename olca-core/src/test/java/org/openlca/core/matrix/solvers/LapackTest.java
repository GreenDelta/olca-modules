package org.openlca.core.matrix.solvers;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.core.DataDir;
import org.openlca.julia.Julia;
import org.openlca.nativelib.NativeLib;

public class LapackTest {

	@BeforeClass
	public static void setup() {
		NativeLib.loadFrom(DataDir.root());
	}

	@Test
	public void testInvert() {
		double[] a = { 1, -4, 0, 2 };
		Julia.invert(2, a);
		Assert.assertArrayEquals(new double[] { 1, 2, 0, 0.5 }, a, 1e-16);
	}

	@Test
	public void testSolve() {
		double[] a = { 1, -4, 0, 2 };
		double[] b = { 1, 0, 0, 1 };
		int info = Julia.solve(2, 2, a, b);
		Assert.assertEquals(0, info);
		Assert.assertArrayEquals(new double[] { 1, 2, 0, 0.5 }, b, 1e-16);
	}
}
