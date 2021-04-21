package org.openlca.core.matrix.format;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CSCMatrixTest {

	@Test
	public void testAllZeros() {
		var hpm = new HashPointMatrix(10, 10);
		var csc = CSCMatrix.of(hpm);
		assertEquals(10, csc.rows);
		assertEquals(10, csc.columns);
		assertEquals(0, csc.rowIndices.length);
		assertEquals(0, csc.values.length);
		assertEquals(11, csc.columnPointers.length);
		for (int i = 0; i < 11; i++) {
			assertEquals(0, csc.columnPointers[i]);
		}
		csc.iterate((row, col, val) -> fail());
	}

	@Test
	public void testEmptyColumn() {
		var hpm = new HashPointMatrix();
		hpm.set(0, 0, 1);
		hpm.set(1, 2, 2);
		var csc = CSCMatrix.of(hpm);
		assertEquals(2, csc.rows);
		assertEquals(3, csc.columns);
		assertEquals(2, csc.rowIndices.length);
		assertEquals(2, csc.values.length);
		assertArrayEquals(v(0, 1, 1, 2), csc.columnPointers);
		csc.iterate((row, col, val) -> {
			if (row == 0 && col == 0) {
				assertEquals(1.0, val, 1e-10);
			} else if (row == 1 && col == 2) {
				assertEquals(2.0, val, 1e-10);
			} else {
				fail();
			}
		});
	}

	@Test
	public void testScaleColumns() {
		var hpm = new HashPointMatrix(2, 3);
		hpm.setValues(new double[][]{
				{1, 2, 3},
				{4, 5, 6},
		});
		var csc = CSCMatrix.of(hpm);
		csc.scaleColumns(v(0.5, 1.0, 1.5));

		var expected = new double[][]{
				{0.5, 2.0, 4.5},
				{2.0, 5.0, 9.0},
		};
		csc.iterate((row, col, val)
				-> assertEquals(expected[row][col], val, 1e-10));
	}

	@Test
	public void testDiagonal() {
		var hmp = new HashPointMatrix();
		hmp.set(0, 0, 1.0);
		hmp.set(1, 1, 2.0);
		hmp.set(2, 2, 3.0);
		var csc = CSCMatrix.of(hmp);

		// test fields
		assertEquals(3, csc.rows);
		assertEquals(3, csc.columns);
		assertArrayEquals(v(1.0, 2.0, 3.0), csc.values, 1e-10);
		assertArrayEquals(v(0, 1, 2), csc.rowIndices);
		assertArrayEquals(v(0, 1, 2, 3), csc.columnPointers);

		// test get
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				if (row == col) {
					assertEquals(row + 1.0, csc.get(row, col), 1e-10);
				} else {
					assertEquals(0.0, csc.get(row, col), 1e-10);
				}
			}
		}

		// test get column
		for (int col = 0; col < 3; col++) {
			double[] column = csc.getColumn(col);
			for (int row = 0; row < 3; row++) {
				if (row == col) {
					assertEquals(row + 1.0, column[row], 1e-10);
				} else {
					assertEquals(0.0, column[row], 1e-10);
				}
			}
		}

		// test get row
		for (int r = 0; r < 3; r++) {
			double[] row = csc.getRow(r);
			for (int col = 0; col < 3; col++) {
				if (r == col) {
					assertEquals(r + 1.0, row[col], 1e-10);
				} else {
					assertEquals(0.0, row[col], 1e-10);
				}
			}
		}

		// test iterate
		csc.iterate((row, col, val)
				-> assertEquals(hmp.get(row, col), val, 1e-10));
	}

	@Test
	public void testMatrix() {

		var data = new double[][] {
			{2, 3, 0, 0, 0},
			{3, 0, 4, 0, 6},
			{0, -1, -3, 2, 0,},
			{0, 0, 1, 0, 0},
			{0, 4, 2, 0, 1,},
		};
		var hpm = HashPointMatrix.of(data);
		var csc = CSCMatrix.of(hpm);

		// test fields
		assertEquals(5, csc.rows);
		assertEquals(5, csc.columns);
		assertArrayEquals(
				v(2., 3., 3., -1., 4., 4., -3., 1., 2., 2., 6., 1.),
				csc.values, 1e-10);
		assertArrayEquals(
				v(0, 1, 0, 2, 4, 1, 2, 3, 4, 2, 1, 4),
				csc.rowIndices);
		assertArrayEquals(
				v(0, 2, 5, 9, 10, 12),
				csc.columnPointers);

		// test get
		for (int r = 0; r < 5; r++) {
			double[] row = new double[5];
			for (int c = 0; c < 5; c++) {
				row[c] = csc.get(r, c);
			}
			assertArrayEquals(data[r], row, 1e-10);
		}

		// test get row/column
		for (int i = 0; i < 5; i++) {
			assertArrayEquals(hpm.getRow(i), csc.getRow(i), 1e-10);
			assertArrayEquals(hpm.getColumn(i), csc.getColumn(i), 1e-10);
		}
	}

	private double[] v(double... vals) {
		return vals;
	}

	private int[] v(int... vals) {
		return vals;
	}

}
