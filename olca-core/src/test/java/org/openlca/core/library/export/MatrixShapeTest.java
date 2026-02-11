package org.openlca.core.library.export;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.JavaMatrix;
import org.openlca.core.matrix.format.MatrixReader;

public class MatrixShapeTest {

	@Test
	public void testExtendDense() {
		var matrix = DenseMatrix.of(new double[][]{
				{1, 3},
				{2, 4}
		});
		checkExtendBoth(matrix.copy());
		checkExtendRows(matrix.copy());
		checkExtendCols(matrix.copy());
	}

	@Test
	public void testExtendHashPoints() {
		var matrix = HashPointMatrix.of(new double[][]{
				{1, 3},
				{2, 4}
		});
		checkExtendBoth(matrix.copy());
		checkExtendRows(matrix.copy());
		checkExtendCols(matrix.copy());
	}

	@Test
	public void testExtendJava() {
		var java = new JavaMatrix(2, 2);
		java.setValues(new double[][]{
				{1, 3},
				{2, 4}
		});
		checkExtendBoth(java.copy());
		checkExtendRows(java.copy());
		checkExtendCols(java.copy());
	}

	private void checkExtendBoth(MatrixReader m) {
		assertEquals(2, m.rows());
		assertEquals(2, m.columns());
		var ext = MatrixShape.ensureIfPresent(m, 3, 3);
		assertEquals(3, ext.rows());
		assertEquals(3, ext.columns());

		assertEquals(1, ext.get(0, 0), 1e-16);
		assertEquals(2, ext.get(1, 0), 1e-16);
		assertEquals(0, ext.get(2, 0), 1e-16);
		assertEquals(3, ext.get(0, 1), 1e-16);
		assertEquals(4, ext.get(1, 1), 1e-16);
		assertEquals(0, ext.get(2, 1), 1e-16);
		assertEquals(0, ext.get(0, 2), 1e-16);
		assertEquals(0, ext.get(1, 2), 1e-16);
		assertEquals(0, ext.get(2, 2), 1e-16);
	}

	private void checkExtendRows(MatrixReader m) {
		assertEquals(2, m.rows());
		assertEquals(2, m.columns());
		var ext = MatrixShape.ensureIfPresent(m, 3, 2);
		assertEquals(3, ext.rows());
		assertEquals(2, ext.columns());

		assertEquals(1, ext.get(0, 0), 1e-16);
		assertEquals(2, ext.get(1, 0), 1e-16);
		assertEquals(0, ext.get(2, 0), 1e-16);
		assertEquals(3, ext.get(0, 1), 1e-16);
		assertEquals(4, ext.get(1, 1), 1e-16);
		assertEquals(0, ext.get(2, 1), 1e-16);
	}

	private void checkExtendCols(MatrixReader m) {
		assertEquals(2, m.rows());
		assertEquals(2, m.columns());
		var ext = MatrixShape.ensureIfPresent(m, 2, 3);
		assertEquals(2, ext.rows());
		assertEquals(3, ext.columns());

		assertEquals(1, ext.get(0, 0), 1e-16);
		assertEquals(2, ext.get(1, 0), 1e-16);
		assertEquals(3, ext.get(0, 1), 1e-16);
		assertEquals(4, ext.get(1, 1), 1e-16);
		assertEquals(0, ext.get(0, 2), 1e-16);
		assertEquals(0, ext.get(1, 2), 1e-16);
	}

}
