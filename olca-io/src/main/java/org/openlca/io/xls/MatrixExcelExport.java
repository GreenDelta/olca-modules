package org.openlca.io.xls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.ByteMatrixReader;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.io.MatrixExport;
import org.openlca.core.matrix.io.index.IxContext;
import org.openlca.core.matrix.io.index.IxEnviIndex;
import org.openlca.core.matrix.io.index.IxFlow;
import org.openlca.core.matrix.io.index.IxImpact;
import org.openlca.core.matrix.io.index.IxImpactIndex;
import org.openlca.core.matrix.io.index.IxLocation;
import org.openlca.core.matrix.io.index.IxProvider;
import org.openlca.core.matrix.io.index.IxTechIndex;

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

		var context = IxContext.of(db);

		// tech. index
		if (data.techIndex != null) {
			var techIdx = IxTechIndex.of(data.techIndex, context);
			sheetOf("index_A", sheet -> {
				header(sheet,
					"Index",
					"Process ID",
					"Process name",
					"Process category",
					"Process location",
					"Flow ID",
					"Flow name",
					"Flow category",
					"Flow unit",
					"Flow type");
				var i = 0;
				for (var item : techIdx.items()) {
					i++;
					var row = sheet.createRow(i);
					Excel.cell(row, 0, item.index());
					next(item.provider(), row);
					next(item.flow(), row, 5);
				}
			});
		}

		// flow index
		if (data.enviIndex != null) {
			var enviIdx = IxEnviIndex.of(data.enviIndex, context);
			sheetOf("index_B", sheet -> {
				header(sheet,
					"Index",
					"Flow ID",
					"Flow name",
					"Flow category",
					"Flow unit",
					"Flow type",
					"Location ID",
					"Location name",
					"Location code");
				int i = 0;
				for (var item : enviIdx.items()) {
					i++;
					var row = sheet.createRow(i);
					Excel.cell(row, 0, item.index());
					next(item.flow(), row, 1);
					next(item.location(), row);
				}
			});
		}

		// impact index
		if (data.impactIndex != null) {
			var impactIdx = IxImpactIndex.of(data.impactIndex);
			sheetOf("index_C", sheet -> {
				header(sheet,
					"Index",
					"Indicator ID",
					"Indicator name",
					"Indicator unit");
				int i = 0;
				for (var item : impactIdx.items()) {
					i++;
					var row = sheet.createRow(i);
					Excel.cell(row, 0, item.index());
					next(item.impact(), row);
				}
			});
		}
	}

	private void sheetOf(String name, Consumer<Sheet> fn) {
		try (var wb = new SXSSFWorkbook(-1)) {
			var sheet = wb.createSheet(name);
			fn.accept(sheet);
			var file = new File(folder, name + ".xlsx");
			try (var stream = new FileOutputStream(file)) {
				wb.write(stream);
				wb.dispose();
			}
		} catch (IOException e) {
			throw new RuntimeException(
				"failed to write matrix " + name, e);
		}
	}

	private void header(Sheet sheet, String... h) {
		var row = sheet.createRow(0);
		for (int i = 0; i < h.length; i++) {
			Excel.cell(row, i, h[i]);
		}
	}

	private void next(IxProvider p, Row row) {
		if (p == null)
			return;
		Excel.cell(row, 1, p.id());
		Excel.cell(row, 2, p.name());
		Excel.cell(row, 3, p.category());
		Excel.cell(row, 4, p.locationCode());
	}

	private void next(IxFlow f, Row row, int offset) {
		if (f == null)
			return;
		Excel.cell(row, offset, f.id());
		Excel.cell(row, offset + 1, f.name());
		Excel.cell(row, offset + 2, f.category());
		Excel.cell(row, offset + 3, f.unit());

		var type = f.type() == null
			? ""
			: switch (f.type()) {
			case ELEMENTARY_FLOW -> "elementary";
			case PRODUCT_FLOW -> "product";
			case WASTE_FLOW -> "waste";
		};
		Excel.cell(row, offset + 4, type);
	}

	private void next(IxLocation loc, Row row) {
		if (loc == null)
			return;
		Excel.cell(row, 5, loc.id());
		Excel.cell(row, 6, loc.name());
		Excel.cell(row, 7, loc.code());
	}

	private void next(IxImpact imp, Row row) {
		if (imp == null)
			return;
		Excel.cell(row, 5, imp.id());
		Excel.cell(row, 6, imp.name());
		Excel.cell(row, 7, imp.unit());
	}
}
