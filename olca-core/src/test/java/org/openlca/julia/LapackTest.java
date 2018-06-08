package org.openlca.julia;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LapackTest {

	@BeforeClass
	public static void setUp() {
		Julia.load();
	}

	@Before
	public void assumeLibsLoaded() {
		// run the tests in this class only if the Julia libraries could be
		// loaded
		assumeTrue(Julia.loaded());
	}

	@Test
	public void testSolve() {
		double[] a = { 1, -4, 0, 2 };
		double[] b = { 1, 0, 0, 1 };
		int info = Julia.solve(2, 2, a, b);
		assertTrue(info == 0);
		assertArrayEquals(new double[] { 1, 2, 0, 0.5 }, b, 1e-16);
	}

}
