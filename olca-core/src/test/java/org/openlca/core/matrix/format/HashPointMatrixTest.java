package org.openlca.core.matrix.format;

import org.junit.Test;

import static org.junit.Assert.*;

public class HashPointMatrixTest {

	@Test
	public void testMultiply() {
		var m = HashPointMatrix.of(new double[][]{
				{1, 2, 3},
				{4, 5, 6}
		});
		double[] x = m.multiply(new double[]{0.5, 1, 1.5});
		assertArrayEquals(new double[]{7, 16}, x, 1e-16);
	}

	@Test
	public void testScaleColumns() {
		var m = HashPointMatrix.of(new double[][]{
				{1, 2, 3},
				{4, 5, 6}
		});
		m.scaleColumns(new double[]{0.5, 1, 1.5});
		assertArrayEquals(new double[]{0.5, 2, 4.5}, m.getRow(0), 1e-16);
		assertArrayEquals(new double[]{2, 5, 9}, m.getRow(1), 1e-16);
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

	@Test
	public void testDrop() {
		var m = new HashPointMatrix();

		// set zeros
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 100; j++) {
				m.set(i, j, 0.0);
			}
		}
		assertEquals(0, m.getNumberOfEntries());
		assertEquals(100, m.columns());
		assertEquals(100, m.rows());

		// set ones
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 100; j++) {
				m.set(i, j, 1.0);
			}
		}
		assertEquals(100 * 100, m.getNumberOfEntries());

		// set zeros
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 100; j++) {
				m.set(i, j, 0.0);
			}
		}
		assertEquals(0, m.getNumberOfEntries());
		assertEquals(100, m.columns());
		assertEquals(100, m.rows());
	}

	@Test
	public void compressDiag() {
		var hashPoints = new HashPointMatrix(100, 100);
		for (int i = 0; i < 100; i++) {
			hashPoints.set(i, i, 1);
		}
		var csc = hashPoints.compress();
		assertEquals(100, csc.columns());
		assertEquals(100, csc.rows());
		for (int i = 0; i < 100; i++) {
			assertEquals(1, csc.get(i, i), 1e-10);
		}
	}

	@Test
	public void compressDense() {
		var hashPoints = new HashPointMatrix();
		for (int i = 0; i < 42; i++) {
			for (int j = 0; j < 24; j++) {
				hashPoints.set(i, j, i * j);
			}
		}
		var csc = hashPoints.compress();
		assertEquals(42, csc.rows());
		assertEquals(24, csc.columns());
		for (int i = 0; i < 42; i++) {
			for (int j = 0; j < 24; j++) {
				assertEquals(i * j, csc.get(i, j), 1e-10);
			}
		}
	}
}
