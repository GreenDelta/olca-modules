package org.openlca.core.matrix.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.openlca.core.matrix.format.IMatrix;

/**
 * Provides methods for writing matrices in the Matrix-Market format
 * (see http://math.nist.gov/MatrixMarket/formats.html).
 */
public class MarketFormatWriter {

	private File file;
	private IMatrix matrix;

	public MarketFormatWriter(File file, IMatrix matrix) {
		this.file = file;
		this.matrix = matrix;
	}

	/**
	 * Writes the matrix in array format:
	 * <ol>
	 * <li>file header</li>
	 * <li>[number of rows] [number of columns]</li>
	 * <li>each entry in a separate row in column-major order</li>
	 * </ol>
	 * See the Matrix-Market format specification for more details.
	 */
	public void writeDense() throws IOException {
		if (file == null || matrix == null)
			return;
		try (Writer w = new FileWriter(file);
		     BufferedWriter buff = new BufferedWriter(w)) {
			buff.write("%%MatrixMarket matrix array real general");
			buff.newLine();
			int rows = matrix.rows();
			int cols = matrix.columns();
			buff.write(rows + " " + cols);
			buff.newLine();
			for (int col = 0; col < cols; col++) {
				for (int row = 0; row < rows; row++) {
					double val = matrix.get(row, col);
					buff.write(Double.toString(val));
					buff.newLine();
				}
			}
		}
	}

	/**
	 * Writes the matrix in coordinate format:
	 * <ol>
	 * <li>file header</li>
	 * <li>[number of rows] [number of columns] [number of entries]</li>
	 * <li>entries in the form: [row index] [column index] [value]</li>
	 * </ol>
	 * See the Matrix-Market format specification for more details.
	 */
	public void writeSparse() throws IOException {
		if (file == null || matrix == null)
			return;
		try (Writer w = new FileWriter(file);
		     BufferedWriter buff = new BufferedWriter(w)) {
			buff.write("%%MatrixMarket matrix coordinate real general");
			buff.newLine();
			int rows = matrix.rows();
			int cols = matrix.columns();
			int entries = countEntries(matrix);
			buff.write(rows + " " + cols + " " + entries);
			buff.newLine();
			for (int col = 0; col < cols; col++) {
				for (int row = 0; row < rows; row++) {
					double val = matrix.get(row, col);
					if (val != 0)
						writeSparseRow(row, col, val, buff);
				}
			}
		}
	}

	private void writeSparseRow(int row, int col, double val,
			BufferedWriter buff) throws IOException {
		int rowIdx = row + 1;
		int colIdx = col + 1;
		buff.write(rowIdx + " " + colIdx + " " + val);
		buff.newLine();
	}

	private int countEntries(IMatrix matrix) {
		int count = 0;
		for (int col = 0; col < matrix.columns(); col++) {
			for (int row = 0; row < matrix.rows(); row++) {
				if (matrix.get(row, col) != 0)
					count++;
			}
		}
		return count;
	}
}
