package org.openlca.core.matrix.io;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.ByteMatrixReader;
import org.openlca.core.matrix.format.MatrixReader;

public class CsvExport extends MatrixExport {

	private Csv csv = Csv.defaultConfig();

	CsvExport(IDatabase db, File folder, MatrixData data) {
		super(db, folder, data);
	}

	public CsvExport configure(Csv csv) {
		if (csv != null) {
			this.csv = csv;
		}
		return this;
	}

	@Override
	public void writeIndices() {
	}

	@Override
	protected void write(MatrixReader matrix, String name) {
		if (matrix == null)
			return;
		var file = new File(folder, name + ".csv");
		csv.write(matrix, file);
	}

	@Override
	protected void write(double[] vector, String name) {
		if (vector == null)
			return;
		var file = new File(folder, name + ".csv");
		csv.write(vector, file);
	}

	@Override
	protected void write(ByteMatrixReader matrix, String name) {
		if (matrix == null)
			return;
		var file = new File(folder, name + ".csv");
		csv.write(matrix, file);
	}
}
