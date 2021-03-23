package org.openlca.core.matrix;

import org.openlca.core.matrix.format.MatrixBuilder;
import org.openlca.core.matrix.format.MatrixReader;

public final class IndexedMatrix<R, C> {

	private final MatrixIndex<R> rows;
	private final MatrixIndex<C> columns;
	private final MatrixReader data;

	private IndexedMatrix(
		MatrixIndex<R> rows,
		MatrixIndex<C> columns,
		MatrixReader data) {
		this.rows = rows;
		this.columns = columns;
		this.data = data;
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

}
