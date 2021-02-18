package org.openlca.io.xls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.ByteMatrixReader;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.io.MatrixExport;

public class MatrixExcelExport extends MatrixExport {

	private MatrixExcelExport(IDatabase db, File folder, MatrixData data) {
		super(db, folder, data);
	}

	@Override
	protected void write(MatrixReader matrix, String name) {
		if (matrix == null || name == null)
			return;
		var wb = new SXSSFWorkbook(-1);
		var sheet = wb.createSheet(name);
		for (int row = 0; row < matrix.rows(); row++) {
			var sheetRow = sheet.createRow(row);
			for (int col = 0; col < matrix.columns(); col++) {
				sheetRow.createCell(col)
					.setCellValue(matrix.get(row, col));
			}
		}
		var file = new File(folder, name + ".xlsx");
		try (var stream = new FileOutputStream(file)) {
			wb.write(stream);
			wb.dispose();
		} catch (IOException e) {
			throw new RuntimeException(
				"failed to write matrix " + name, e);
		}
	}

	@Override
	protected void write(double[] vector, String name) {

	}

	@Override
	protected void write(ByteMatrixReader matrix, String name) {

	}

	@Override
	public void writeIndices() {

	}
}
