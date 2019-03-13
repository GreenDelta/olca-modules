package org.openlca.julia;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assume.assumeTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openlca.core.matrix.format.CCRMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;

public class UmfpackTest {

	@BeforeClass
	public static void setUp() {
		Julia.load();
	}

	@Before
	public void assumeLibsLoaded() {
		// run the tests in this class only if the Julia libraries could be
		// loaded
		assumeTrue(Julia.isLoaded());
		assumeTrue(Julia.isWithUmfpack());
	}

	@Test
	public void testSolveNative() {
		double[] x = new double[5];
		Julia.umfSolve(5,
				new int[] { 0, 2, 5, 9, 10, 12 },
				new int[] { 0, 1, 0, 2, 4, 1, 2, 3, 4, 2, 1, 4 },
				new double[] { 2., 3., 3., -1., 4., 4., -3., 1., 2., 2., 6.,
						1. },
				new double[] { 8., 45., -3., 3., 19. },
				x);
		assertArrayEquals(
				new double[] { 1d, 2d, 3d, 4d, 5d }, x, 1e-8);
	}

	@Test
	public void testSolveMatrix() {
		HashPointMatrix m = new HashPointMatrix(new double[][] {
				{ 2.0, 3.0, 0.0, 0.0, 0.0 },
				{ 3.0, 0.0, 4.0, 0.0, 6.0 },
				{ 0.0, -1.0, -3.0, 2.0, 0.0 },
				{ 0.0, 0.0, 1.0, 0.0, 0.0 },
				{ 0.0, 4.0, 2.0, 0.0, 1.0 } });
		CCRMatrix uMatrix = CCRMatrix.of(m);
		double[] demand = { 8., 45., -3., 3., 19. };
		double[] x = Umfpack.solve(uMatrix, demand);
		assertArrayEquals(
				new double[] { 1d, 2d, 3d, 4d, 5d }, x, 1e-8);
	}

	@Test
	@Ignore
	public void testFactorizeMatrix() {
		HashPointMatrix m = new HashPointMatrix(new double[][] {
				{ 2.0, 3.0, 0.0, 0.0, 0.0 },
				{ 3.0, 0.0, 4.0, 0.0, 6.0 },
				{ 0.0, -1.0, -3.0, 2.0, 0.0 },
				{ 0.0, 0.0, 1.0, 0.0, 0.0 },
				{ 0.0, 4.0, 2.0, 0.0, 1.0 } });
		CCRMatrix uMatrix = CCRMatrix.of(m);
		UmfFactorizedMatrix factorizedM = Umfpack.factorize(uMatrix);

		double[] demand = { 8., 45., -3., 3., 19. };
		double[] x = Umfpack.solve(factorizedM, demand);
		assertArrayEquals(
				new double[] { 1d, 2d, 3d, 4d, 5d }, x, 1e-8);
		factorizedM.dispose();
	}
}
