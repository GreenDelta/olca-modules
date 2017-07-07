package org.openlca.core.matrix.format;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.matrix.format.CompressedRowMatrix;
import org.openlca.core.matrix.format.HashMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompressedRowMatrixTest {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void testSetAndClearEntries() {
		int rows = (int) (150 * Math.random() + 1);
		int cols = (int) (150 * Math.random() + 1);
		log.trace("test a {} x {} matrix", rows, cols);
		CompressedRowMatrix m = new CompressedRowMatrix(rows, cols);
		setAllValues(m, 1);
		assertAllValues(m, 1);
		Assert.assertEquals(rows * cols, m.columnIndices.length);
		Assert.assertEquals(rows * cols, m.values.length);
		setAllValues(m, 0);
		assertAllValues(m, 0);
		Assert.assertEquals(0, m.columnIndices.length);
		Assert.assertEquals(0, m.values.length);
	}

	private void setAllValues(CompressedRowMatrix m, double val) {
		for (int row = 0; row < m.rows; row++) {
			for (int col = 0; col < m.columns; col++)
				m.set(row, col, val);
		}
	}

	private void assertAllValues(CompressedRowMatrix m, double val) {
		for (int row = 0; row < m.rows; row++) {
			double[] expectedRow = new double[m.columns];
			for (int col = 0; col < m.columns; col++)
				expectedRow[col] = val;
			Assert.assertArrayEquals(expectedRow, m.getRow(row), 1e-24);
		}
		for (int col = 0; col < m.columns; col++) {
			double[] expectedColumn = new double[m.rows];
			for (int row = 0; row < m.rows; row++)
				expectedColumn[row] = val;
			Assert.assertArrayEquals(expectedColumn, m.getColumn(col), 1e-24);
		}
		for (int row = 0; row < m.rows; row++) {
			for (int col = 0; col < m.columns; col++)
				Assert.assertEquals(val, m.get(row, col), 1e-24);
		}
	}

	@Test
	public void testDiagonalMatrix() {
		int n = (int) (150 * Math.random() + 1);
		log.trace("test a {} x {} matrix", n, n);
		CompressedRowMatrix m = new CompressedRowMatrix(n, n);
		for (int i = 0; i < n; i++)
			m.set(i, i, 1);
		testIdentityMatrix(n, m);
	}

	@Test
	public void testCompressSparseHashMatrix() {
		int n = (int) (150 * Math.random() + 1);
		log.trace("test a {} x {} matrix", n, n);
		HashMatrix hashMatrix = new HashMatrix(n, n);
		for (int i = 0; i < n; i++)
			hashMatrix.set(i, i, 1);
		CompressedRowMatrix m = hashMatrix.compress();
		testIdentityMatrix(n, m);
	}

	private void testIdentityMatrix(int n, CompressedRowMatrix m) {
		Assert.assertEquals(n, m.columnIndices.length);
		Assert.assertEquals(n, m.values.length);
		for (int row = 0; row < n; row++) {
			double[] vals = m.getRow(row);
			double[] expexted = new double[n];
			expexted[row] = 1;
			Assert.assertArrayEquals(expexted, vals, 1e-24);
		}
		for (int col = 0; col < n; col++) {
			double[] vals = m.getColumn(col);
			double[] expexted = new double[n];
			expexted[col] = 1;
			Assert.assertArrayEquals(expexted, vals, 1e-24);
		}
	}

}
