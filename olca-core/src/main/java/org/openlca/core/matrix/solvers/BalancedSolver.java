package org.openlca.core.matrix.solvers;

import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.HashMatrix;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.format.HashMatrix.MatrixIterator;
import org.openlca.eigen.Eigen;
import org.openlca.core.matrix.format.MatrixConverter;
import org.openlca.core.matrix.format.SparseMatrixData;

public class BalancedSolver implements IMatrixSolver {

	private DenseSolver denseSolver = new DenseSolver();

	@Override
	public IMatrix matrix(int rows, int columns) {
		return new HashMatrix(rows, columns);
	}

	@Override
	public double[] solve(IMatrix a, int idx, double d) {
		if (a.columns() == 1)
			return new double[] { d / a.get(0, 0) }; // see sparseLU doc
		if (a instanceof DenseMatrix)
			return denseSolver.solve(a, idx, d);
		HashMatrix A = MatrixConverter.asHashMatrix(a);
		SparseMatrixData aData = new SparseMatrixData(A);
		double[] b = new double[a.rows()];
		b[idx] = d;
		double[] x = new double[aData.rows];
		Eigen.sparseLu(aData.columns, aData.numberOfEntries, aData.rowIndices,
				aData.columnIndices, aData.values, b, x);
		return x;
	}

	@Override
	public double[] multiply(IMatrix m, final double[] v) {
		if (m instanceof DenseMatrix)
			return denseSolver.multiply(m, v);
		final double[] x = new double[m.rows()];
		HashMatrix a = MatrixConverter.asHashMatrix(m);
		a.iterate(new MatrixIterator() {
			@Override
			public void next(int row, int col, double val) {
				x[row] += val * v[col];
			}
		});
		return x;
	}

	@Override
	public IMatrix invert(IMatrix a) {
		return denseSolver.invert(a);
	}

	@Override
	public IMatrix multiply(IMatrix a, IMatrix b) {
		return denseSolver.multiply(a, b);
	}

	@Override
	public void scaleColumns(IMatrix m, double[] v) {
		for (int row = 0; row < m.rows(); row++) {
			for (int col = 0; col < m.columns(); col++) {
				m.set(row, col, v[col] * m.get(row, col));
			}
		}
	}

}
