package org.openlca.core.matrix.format;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CCRMatrixTest {

	@Test
	public void testDiagonal() {
		HashPointMatrix m = new HashPointMatrix();
		m.set(0, 0, 1.0);
		m.set(1, 1, 2.0);
		m.set(2, 2, 3.0);
		CCRMatrix ccr = CCRMatrix.of(m);

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

	private double[] v(double... vals) {
		return vals;
	}
	private int[] v(int... vals) {
		return vals;
	}


}
