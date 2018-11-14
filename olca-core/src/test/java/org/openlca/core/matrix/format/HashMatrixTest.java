package org.openlca.core.matrix.format;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class HashMatrixTest {

	@Test
	public void testMultiply() {
		HashMatrix m = new HashMatrix(new double[][] {
				{ 1, 2, 3 },
				{ 4, 5, 6 }
		});
		double[] x = m.multiply(new double[] { 0.5, 1, 1.5 });
		assertArrayEquals(new double[] { 7, 16 }, x, 1e-16);
	}

	@Test
	public void testScaleColumns() {
		HashMatrix m = new HashMatrix(new double[][] {
				{ 1, 2, 3 },
				{ 4, 5, 6 }
		});
		m.scaleColumns(new double[] { 0.5, 1, 1.5 });
		assertArrayEquals(new double[] { 0.5, 2, 4.5 }, m.getRow(0), 1e-16);
		assertArrayEquals(new double[] { 2, 5, 9 }, m.getRow(1), 1e-16);
	}
}
