package org.openlca.io.xls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.ByteMatrixReader;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.io.MatrixExport;

public class MatrixExcelExport extends MatrixExport {

	private boolean skipZeros = true;

	public MatrixExcelExport(IDatabase db, File folder, MatrixData data) {
		super(db, folder, data);
	}

	public MatrixExcelExport skipZeros(boolean b) {
		this.skipZeros = b;
		return this;
	}

	@Override
	protected void write(MatrixReader matrix, String name) {
		if (matrix == null || name == null)
			return;
		sheetOf(name, sheet -> {
			for (int i = 0; i < matrix.rows(); i++) {
				var row = sheet.createRow(i);
				for (int j = 0; j < matrix.columns(); j++) {
					double val = matrix.get(i, j);
					if (skipZeros && val == 0)
						continue;
					row.createCell(j).setCellValue(val);
				}
			}
		});
	}

	@Override
	protected void write(double[] vector, String name) {
		if (vector == null || name == null)
			return;
		sheetOf(name, sheet -> {
			for (int i = 0; i < vector.length; i++) {
				sheet.createRow(i)
					.createCell(0)
					.setCellValue(vector[i]);
			}
		});
	}

	@Override
	protected void write(ByteMatrixReader matrix, String name) {
		if (matrix == null || name == null)
			return;
		sheetOf(name, sheet -> {
			for (int i = 0; i < matrix.rows(); i++) {
				var row = sheet.createRow(i);
				for (int j = 0; j < matrix.columns(); j++) {
					byte val = matrix.get(i, j);
					if (skipZeros && val == 0)
						continue;
					row.createCell(j).setCellValue(val);
				}
			}
		});
	}

	@Override
	public void writeIndices() {
		BiConsumer<Row, String[]> cells = (row, values) -> {
			if (values == null)
				return;
			for (int j = 0; j < values.length; j++) {
				var val = values[j];
				if (val != null) {
					row.createCell(j).setCellValue(values[j]);
				}
			}
		};

		// tech. index
		if (data.techIndex != null) {
			sheetOf("index_A", sheet -> {
				var i = new AtomicInteger(0);
				eachTechIndexRow(values -> {
					var row = sheet.createRow(i.getAndIncrement());
					cells.accept(row, values);
				});
			});
		}

		// flow index
		if (data.flowIndex != null) {
			sheetOf("index_B", sheet -> {
				var i = new AtomicInteger(0);
				eachFlowIndexRow(values -> {
					var row = sheet.createRow(i.getAndIncrement());
					cells.accept(row, values);
				});
			});
		}

		// impact index
		if (data.impactIndex != null) {
			sheetOf("index_C", sheet -> {
				var i = new AtomicInteger(0);
				eachImpactIndexRow(values -> {
					var row = sheet.createRow(i.getAndIncrement());
					cells.accept(row, values);
				});
			});
		}
	}

	private void sheetOf(String name, Consumer<Sheet> fn) {
		var wb = new SXSSFWorkbook(-1);
		var sheet = wb.createSheet(name);
		fn.accept(sheet);
		var file = new File(folder, name + ".xlsx");
		try (var stream = new FileOutputStream(file)) {
			wb.write(stream);
			wb.dispose();
		} catch (IOException e) {
			throw new RuntimeException(
				"failed to write matrix " + name, e);
		}
	}
}
