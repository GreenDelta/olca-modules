package org.openlca.core.matrix.io;

import java.io.BufferedWriter;
import java.io.File;
import java.util.function.BiConsumer;

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
		BiConsumer<BufferedWriter, String[]> writeln = (w, row) -> {
			for (int i = 0; i < row.length; i++) {
				row[i] = csv.quote(row[i]);
			}
			csv.writeln(w, csv.line(row));
		};

		// tech. index
		if (data.techIndex != null) {
			var indexA = new File(folder, "index_A.csv");
			csv.writer(
				indexA,
				w -> eachTechIndexRow(row -> writeln.accept(w, row)));
		}

		// flow index
		if (data.enviIndex != null) {
			var indexB = new File(folder, "index_B.csv");
			csv.writer(
				indexB,
				w -> eachFlowIndexRow(row -> writeln.accept(w, row)));
		}

		// impact index
		if (data.impactIndex != null) {
			var indexC = new File(folder, "index_C.csv");
			csv.writer(
				indexC,
				w -> eachImpactIndexRow(row -> writeln.accept(w, row)));
		}
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
