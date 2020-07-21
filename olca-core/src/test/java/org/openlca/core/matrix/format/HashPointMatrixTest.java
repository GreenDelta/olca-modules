package org.openlca.core.matrix.format;

import org.junit.Test;

import static org.junit.Assert.*;

public class HashPointMatrixTest {

	@Test
	public void testMultiply() {
		var m = new HashPointMatrix(new double[][] {
				{ 1, 2, 3 },
				{ 4, 5, 6 }
		});
		double[] x = m.multiply(new double[] { 0.5, 1, 1.5 });
		assertArrayEquals(new double[] { 7, 16 }, x, 1e-16);
	}

	@Test
	public void testScaleColumns() {
		var m = new HashPointMatrix(new double[][] {
				{ 1, 2, 3 },
				{ 4, 5, 6 }
		});
		m.scaleColumns(new double[] { 0.5, 1, 1.5 });
		assertArrayEquals(new double[] { 0.5, 2, 4.5 }, m.getRow(0), 1e-16);
		assertArrayEquals(new double[] { 2, 5, 9 }, m.getRow(1), 1e-16);
	}

	@Test
	public void testGetColumn() {
		var m = new HashPointMatrix();
		m.set(0, 0, 1.0);
		m.set(0, 2, 2.0);
		m.set(1, 1, 3.0);
		assertArrayEquals(new double[]{1.0, 0.0}, m.getColumn(0), 1e-16);
		assertArrayEquals(new double[]{0.0, 3.0}, m.getColumn(1), 1e-16);
		assertArrayEquals(new double[]{2.0, 0.0}, m.getColumn(2), 1e-16);
	}

	@Test
	public void testGetRow() {
		var m = new HashPointMatrix();
		m.set(0, 0, 1.0);
		m.set(0, 2, 2.0);
		m.set(1, 1, 3.0);
		assertArrayEquals(new double[]{1.0, 0.0, 2.0}, m.getRow(0), 1e-16);
		assertArrayEquals(new double[]{0.0, 3.0, 0.0}, m.getRow(1), 1e-16);
	}

	@Test
	public void testCopy() {
		var m = new HashPointMatrix();
		m.set(0, 0, 1.0);
		m.set(0, 2, 2.0);
		m.set(1, 1, 3.0);
		var copy = m.copy();
		assertEquals(2, copy.rows());
		assertEquals(3, copy.columns());
		copy.iterate((row, col, val) -> {
			if (row == 0 && col == 0) {
				assertEquals(1.0, val, 1e-16);
			} else if (row == 0 && col == 2) {
				assertEquals(2.0, val, 1e-16);
			} else if (row == 1 && col == 1) {
				assertEquals(3.0, val, 1e-16);
			} else {
				fail();
			}
		});
	}
}
