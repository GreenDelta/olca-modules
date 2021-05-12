package org.openlca.core.matrix;

import java.util.Objects;

import org.openlca.core.matrix.format.MatrixBuilder;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.index.MatrixIndex;

public final class IndexedMatrix<R, C> {

	private final MatrixIndex<R> rows;
	private final MatrixIndex<C> columns;
	private final MatrixReader data;

	private IndexedMatrix(
		MatrixIndex<R> rows,
		MatrixIndex<C> columns,
		MatrixReader data) {
		this.rows = Objects.requireNonNull(rows);
		this.columns = Objects.requireNonNull(columns);
		this.data = Objects.requireNonNull(data);
	}

	public static <I> IndexedMatrix<I, I> of(
		MatrixIndex<I> index, MatrixReader data) {
		return of(index, index, data);
	}

	public static <R, C> IndexedMatrix<R, C> of(
		MatrixIndex<R> rows,
		MatrixIndex<C> columns,
		MatrixReader data) {
		return new IndexedMatrix<>(rows, columns, data);
	}

	public MatrixIndex<R> rows() {
		return rows;
	}

	public MatrixIndex<C> columns() {
		return columns;
	}

	public MatrixReader data() {
		return data;
	}

	/**
	 * Copies the matching entries of this matrix for the given
	 * row and column index into a new matrix.
	 */
	public IndexedMatrix<R, C> reshape(
		MatrixIndex<R> newRows, MatrixIndex<C> newColumns) {
		var builder = new MatrixBuilder();
		builder.minSize(newRows.size(), newColumns.size());
		data.iterate((row, col, value) -> {
			if (value == 0)
				return;
			var newRow = newRows.of(rows.at(row));
			if (newRow < 0)
				return;
			var newCol = newColumns.of(columns.at(col));
			if (newCol < 0)
				return;
			builder.set(newRow, newCol, value);
		});
		var matrix = builder.finish();
		return new IndexedMatrix<>(newRows, newColumns, matrix);
	}

	public static <I> Builder<I, I> build(MatrixIndex<I> index) {
		return build(index, index);
	}

	public static <R, C> Builder<R, C> build(
		MatrixIndex<R> rows, MatrixIndex<C> columns) {
		return new Builder<>(rows, columns);
	}

	public static class Builder<R, C> {

		private final MatrixIndex<R> rows;
		private final MatrixIndex<C> columns;
		private final MatrixBuilder buffer;

		private Builder(
			MatrixIndex<R> rows,
			MatrixIndex<C> columns) {
			this.rows = Objects.requireNonNull(rows);
			this.columns = Objects.requireNonNull(columns);
			buffer = new MatrixBuilder();
			buffer.minSize(rows.size(), columns.size());
		}

		public Builder<R, C> put(IndexedMatrix<R, C> matrix) {
			if (matrix == null)
				return this;
			int[] rowMap = new int[matrix.rows.size()];
			matrix.rows.each(
				(i, elem) -> rowMap[i] = rows.of(elem));
			int[] colMap = new int[matrix.columns.size()];
			matrix.columns.each(
				(j, elem) -> colMap[j] = columns.of(elem));

			matrix.data.iterate((row, col, val) -> {
				int mappedRow = rowMap[row];
				if (mappedRow < 0)
					return;
				int mappedCol = colMap[col];
				if (mappedCol < 0)
					return;
				buffer.set(mappedRow, mappedCol, val);
			});
			return this;
		}

		public IndexedMatrix<R, C> finish() {
			return IndexedMatrix.of(
				rows, columns, buffer.finish());
		}
	}
}
