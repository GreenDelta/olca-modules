package org.openlca.core.matrix.solvers;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.SparseMatrixData;
import org.openlca.eigen.Eigen;

public class EigenTest {

	static {
		TestSession.loadLib();
	}

	@Test
	public void testSparseLu() {
		HashPointMatrix a = new HashPointMatrix(2, 2);
		a.set(0, 0, 1);
		a.set(1, 0, -2);
		a.set(1, 1, 1);
		SparseMatrixData data = new SparseMatrixData(a);
		double[] b = new double[2];
		b[0] = 1;
		double[] x = new double[2];
		Eigen.sparseLu(2, data.numberOfEntries, data.rowIndices,
				data.columnIndices, data.values, b, x);
		Assert.assertArrayEquals(new double[] { 1, 2 }, x, 1e-14);
	}

	@Test
	@Ignore
	public void testBicgstab() {
		HashPointMatrix a = new HashPointMatrix(2, 2);
		a.set(0, 0, 1);
		a.set(1, 0, -2);
		a.set(1, 1, 1);
		SparseMatrixData data = new SparseMatrixData(a);
		double[] b = new double[2];
		b[0] = 1;
		double[] x = new double[2];
		Eigen.bicgstab(2, data.numberOfEntries, data.rowIndices,
				data.columnIndices, data.values, b, x);
		Assert.assertArrayEquals(new double[] { 1, 2 }, x, 1e-14);
	}

	@Test
	public void testSparseLuInvert() {
		HashPointMatrix a = new HashPointMatrix(2, 2);
		a.set(0, 0, 1);
		a.set(1, 0, -2);
		a.set(1, 1, 1);
		SparseMatrixData data = new SparseMatrixData(a);
		double[] inverse = new double[4];
		Eigen.sparseLuInvert(2, data.numberOfEntries, data.rowIndices,
				data.columnIndices, data.values, inverse);
		Assert.assertArrayEquals(new double[] { 1, 2, 0, 1 }, inverse, 1e-14);
	}

}
