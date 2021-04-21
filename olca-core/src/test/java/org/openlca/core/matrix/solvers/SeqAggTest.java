package org.openlca.core.matrix.solvers;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.matrix.format.HashPointMatrix;

public class SeqAggTest {

	@Test
	public void testSimpleSystem() {
		var a = HashPointMatrix.of(new double[][]{
			{2., -5., 0., 0., 0.},
			{0., 1., -2., 0., 0.},
			{0., 0., 1., 0., -6.},
			{0., 0., -1., 4., 0.},
			{0., 0., 0., 0., 1.},
		});
		var b = HashPointMatrix.of(new double[][]{
			{3, 2, 1, 1, 3},
			{1, 2, 2, 1, 1}
		});
		SeqAgg seqAgg = new SeqAgg(a, b, 4, 1);
		double[] g = seqAgg.solve();
		Assert.assertArrayEquals(new double[]{124.5, 68.5}, g, 1e-16);
	}

	@Test
	public void testLoopSystem() {
		var a = HashPointMatrix.of(new double[][]{
			{4., -2., 0., 0.},
			{0., 1., -0.1, -2.},
			{0., -1., 2., 0.},
			{0., 0., 0., 1.}
		});
		var b = HashPointMatrix.of(new double[][]{
			{2., 3., 2., 1.},
			{3., 1., 2., 1.,}
		});
		SeqAgg seqAgg = new SeqAgg(a, b, 3, 1);
		double[] g = seqAgg.solve();
		Assert.assertArrayEquals(new double[]{11.526316, 8.3684211}, g, 1e-6);
	}

}
