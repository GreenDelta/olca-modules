package org.openlca.core.matrix.format;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CSCMatrixTest {

	@Test
	public void testDiagonal() {
		HashPointMatrix m = new HashPointMatrix();
		m.set(0, 0, 1.0);
		m.set(1, 1, 2.0);
		m.set(2, 2, 3.0);
		CSCMatrix ccr = CSCMatrix.of(m);

		// test fields
		assertEquals(3, ccr.rows);
		assertEquals(3, ccr.columns);
		assertArrayEquals(v(1.0, 2.0, 3.0), ccr.values, 1e-10);
		assertArrayEquals(v(0, 1, 2), ccr.rowIndices);
		assertArrayEquals(v(0, 1, 2, 3), ccr.columnPointers);

		// test get
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				if (row == col) {
					assertEquals(row + 1.0, ccr.get(row, col), 1e-10);
				} else {
					assertEquals(0.0, ccr.get(row, col), 1e-10);
				}
			}
		}

		// test get column
		for (int col = 0; col < 3; col++) {
			double[] column = ccr.getColumn(col);
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
			double[] row = ccr.getRow(r);
			for (int col = 0; col < 3; col++) {
				if (r == col) {
					assertEquals(r + 1.0, row[col], 1e-10);
				} else {
					assertEquals(0.0, row[col], 1e-10);
				}
			}
		}
	}

	@Test
	public void testMatrix() {
		double[][] data = {
				{ 2, 3, 0, 0, 0 },
				{ 3, 0, 4, 0, 6 },
				{ 0, -1, -3, 2, 0, },
				{ 0, 0, 1, 0, 0 },
				{ 0, 4, 2, 0, 1, },
		};
		HashPointMatrix hpm = new HashPointMatrix(data);

		CSCMatrix csc = CSCMatrix.of(hpm);

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
