package org.openlca.io.xls.results.system;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.io.xls.results.CellWriter;

abstract class ContributionMatrix<C, R> {

	private final ResultExport export;
	private final CellWriter writer;
	private final String[] colHeaders;
	private final String[] rowHeaders;

	ContributionMatrix(
			ResultExport export, String[] colHeaders, String[] rowHeaders) {
		this.export = export;
		this.writer = export.writer;
		this.colHeaders = colHeaders;
		this.rowHeaders = rowHeaders;
	}

	void header(Sheet sheet) {
		writer.headerCol(sheet, 1, rowHeaders.length + 1, colHeaders);
		writer.headerRow(sheet, colHeaders.length + 1, 1, rowHeaders);
	}

	void subHeaders(Sheet sheet, List<C> colData, List<R> rowData) {
		int row = colHeaders.length + 2;
		for (R desc : rowData) {
			subHeaderRow(desc, sheet, row++);
		}
		int col = rowHeaders.length + 2;
		for (C desc : colData) {
			subHeaderCol(desc, sheet, col++);
		}
	}

	protected abstract void subHeaderRow(R descriptor, Sheet sheet, int row);

	protected abstract void subHeaderCol(C descriptor, Sheet sheet, int col);

	void data(Sheet sheet, List<C> colData, List<R> rowData) {
		int row = colHeaders.length + 2;
		for (R rowDesc : rowData) {
			if (export.wasCancelled())
				break;
			int col = rowHeaders.length + 2;
			for (C colDesc : colData) {
				if (export.wasCancelled())
					break;
				double val = getValue(colDesc, rowDesc);
				if (val != 0) {
					// do not write zeros in the sheets -> makes the workbook
					// much smaller (and also the export much faster)
					writer.cell(sheet, row, col, val);
				}
				col++;
			}
			row++;
		}
	}

	protected abstract double getValue(C colDesc, R rowDesc);

}
