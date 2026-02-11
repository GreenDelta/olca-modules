package org.openlca.core.matrix.format;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

public class HashPointMatrixTest {

	@Test
	public void testAdd() {
		var m = new HashPointMatrix();

		// basic addition & expansion
		m.add(1, 2, 3.0);
		assertEquals(2, m.rows());
		assertEquals(3, m.columns());
		assertEquals(3.0, m.get(1, 2), 1e-16);
		assertEquals(1, m.getNumberOfEntries());

		// add to existing cell
		m.add(1, 2, 2.0);
		assertEquals(5.0, m.get(1, 2), 1e-16);
		assertEquals(1, m.getNumberOfEntries());

		// add zero (should do nothing)
		m.add(1, 2, 0.0);
		assertEquals(5.0, m.get(1, 2), 1e-16);
		assertEquals(1, m.getNumberOfEntries());

		// add resulting in zero (should remove entry)
		m.add(1, 2, -5.0);
		assertEquals(0.0, m.get(1, 2), 1e-16);
		assertEquals(0, m.getNumberOfEntries());

		// add to new far away cell
		m.add(10, 10, 42.0);
		assertEquals(11, m.rows());
		assertEquals(11, m.columns());
		assertEquals(42.0, m.get(10, 10), 1e-16);
		assertEquals(1, m.getNumberOfEntries());
	}

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
		var csc = hashPoints.pack();
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
		var csc = hashPoints.pack();
		assertEquals(42, csc.rows());
		assertEquals(24, csc.columns());
		for (int i = 0; i < 42; i++) {
			for (int j = 0; j < 24; j++) {
				assertEquals(i * j, csc.get(i, j), 1e-10);
			}
		}
	}

	@Test
	public void testReadColumn() {
		var m = new HashPointMatrix();
		m.set(1, 0, 10.0);
		m.set(3, 0, 30.0);

		double[] buffer = new double[5];
		m.readColumn(0, buffer);
		assertArrayEquals(new double[]{0.0, 10.0, 0.0, 30.0, 0.0}, buffer, 1e-16);

		// column with no data should clear the buffer
		Arrays.fill(buffer, -1.0);
		m.readColumn(5, buffer);
		assertArrayEquals(new double[]{0.0, 0.0, 0.0, 0.0, 0.0}, buffer, 1e-16);
	}

	@Test
	public void testGetOutOfBounds() {
		var m = new HashPointMatrix();
		assertEquals(0.0, m.get(100, 100), 1e-16);
		assertEquals(0, m.rows()); // get should not expand
		assertEquals(0, m.columns());
	}

	@Test
	public void testIterate() {
		var m = new HashPointMatrix();
		m.set(0, 0, 1.0);
		m.set(1, 1, 2.0);

		int[] count = {0};
		double[] sum = {0.0};
		m.iterate((r, c, v) -> {
			count[0]++;
			sum[0] += v;
		});

		assertEquals(2, count[0]);
		assertEquals(3.0, sum[0], 1e-16);
	}

	@Test
	public void testCompressWithEmptyColumns() {
		var m = new HashPointMatrix();
		m.set(0, 0, 1.0); // column 0
		m.set(0, 2, 3.0); // column 2, column 1 is empty

		var csc = m.pack();
		assertEquals(3, csc.columns());
		assertEquals(0, csc.columnPointers[0]); // col 0 start
		assertEquals(1, csc.columnPointers[1]); // col 1 empty, so same as next
		assertEquals(1, csc.columnPointers[2]); // col 2 start
		assertEquals(2, csc.columnPointers[3]); // end
		assertEquals(2, csc.values.length);
	}
}
