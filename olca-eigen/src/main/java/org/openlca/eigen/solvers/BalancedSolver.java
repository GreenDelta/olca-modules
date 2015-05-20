package org.openlca.eigen.solvers;

import org.openlca.core.math.IMatrix;
import org.openlca.core.math.IMatrixFactory;
import org.openlca.core.math.IMatrixSolver;
import org.openlca.eigen.DenseMatrix;
import org.openlca.eigen.Eigen;
import org.openlca.eigen.HashMatrix;
import org.openlca.eigen.HashMatrix.MatrixIterator;
import org.openlca.eigen.HashMatrixFactory;
import org.openlca.eigen.MatrixConverter;
import org.openlca.eigen.SparseMatrixData;

public class BalancedSolver implements IMatrixSolver {

	private DenseSolver denseSolver = new DenseSolver();

	@Override
	public double[] solve(IMatrix a, int idx, double d) {
		if (a.getColumnDimension() == 1)
			return new double[] { d / a.getEntry(0, 0) }; // see sparseLU doc
		if (a instanceof DenseMatrix)
			return denseSolver.solve(a, idx, d);
		HashMatrix A = MatrixConverter.asHashMatrix(a);
		SparseMatrixData aData = new SparseMatrixData(A);
		double[] b = new double[a.getRowDimension()];
		b[idx] = d;
		double[] x = new double[aData.rows];
		Eigen.sparseLu(aData.columns, aData.numberOfEntries, aData.rowIndices,
				aData.columnIndices, aData.values, b, x);
		return x;
	}

	@Override
	public IMatrix transpose(IMatrix m) {
		return denseSolver.transpose(m);
	}

	@Override
	public IMatrix solve(IMatrix a, IMatrix b) {
		// FIXME use sparse matrix if needed
		return denseSolver.solve(a, b);
	}

	@Override
	public double[] multiply(IMatrix m, final double[] v) {
		if (m instanceof DenseMatrix)
			return denseSolver.multiply(m, v);
		final double[] x = new double[m.getRowDimension()];
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
		for (int row = 0; row < m.getRowDimension(); row++) {
			for (int col = 0; col < m.getColumnDimension(); col++) {
				m.setEntry(row, col, v[col] * m.getEntry(row, col));
			}
		}
	}

	@Override
	public IMatrixFactory<?> getMatrixFactory() {
		return new HashMatrixFactory();
	}

}
