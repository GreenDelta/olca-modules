package org.openlca.core.matrix.format;

public interface ColumnIterator {

	static ColumnIterator of(MatrixReader m, int col) {
		if (m == null || col < 0 || col >= m.columns())
			return new NoOp(col);
		if (m instanceof DenseMatrix dense)
			return new DenseIterator(dense, col);
		if (m instanceof HashPointMatrix points)
			return new HashPointIterator(points, col);
		if (m instanceof CSCMatrix csc)
			return new CscIterator(csc, col);
		return new DefaultIterator(m, col);
	}

	int column();

	void eachNonZero(EntryConsumer fn);

	record NoOp(int column) implements ColumnIterator {

		@Override
		public void eachNonZero(EntryConsumer fn) {
		}
	}

	record DefaultIterator(MatrixReader matrix, int column)
			implements ColumnIterator {

		@Override
		public void eachNonZero(EntryConsumer fn) {
			for (int row = 0; row < matrix.rows(); row++) {
				double value = matrix.get(row, column);
				if (value != 0) {
					fn.next(row, value);
				}
			}
		}
	}

	record DenseIterator(DenseMatrix matrix, int column)
			implements ColumnIterator {

		@Override
		public void eachNonZero(EntryConsumer fn) {
			int offset = column * matrix.rows;
			for (int row = 0; row < matrix.rows; row++) {
				double value = matrix.data[offset + row];
				if (value != 0) {
					fn.next(row, value);
				}
			}
		}
	}

	record HashPointIterator(HashPointMatrix matrix, int column)
			implements ColumnIterator {

		@Override
		public void eachNonZero(EntryConsumer fn) {
			var values = matrix.data.get(column);
			if (values == null)
				return;
			for (var it = values.iterator(); it.hasNext(); ) {
				it.advance();
				double value = it.value();
				if (value != 0) {
					fn.next(it.key(), value);
				}
			}
		}
	}

	record CscIterator(CSCMatrix matrix, int column)
			implements ColumnIterator {

		@Override
		public void eachNonZero(EntryConsumer fn) {
			int start = matrix.columnPointers[column];
			int end = column == (matrix.columns - 1)
					? matrix.rowIndices.length
					: matrix.columnPointers[column + 1];
			for (int i = start; i < end; i++) {
				double value = matrix.values[i];
				if (value != 0) {
					fn.next(matrix.rowIndices[i], value);
				}
			}
		}
	}

	record ArrayIterator(double[] values, int column)
			implements ColumnIterator {

		@Override
		public void eachNonZero(EntryConsumer fn) {
			for (int row = 0; row < values.length; row++) {
				var value = values[row];
				if (value != 0) {
					fn.next(row, value);
				}
			}
		}
	}

	@FunctionalInterface
	interface EntryConsumer {
		void next(int row, double value);
	}
}
