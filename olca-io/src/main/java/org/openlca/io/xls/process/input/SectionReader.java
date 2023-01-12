package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.io.xls.Excel;
import org.openlca.io.xls.process.Field;

class SectionReader implements CellReader {

	private final Sheet sheet;
	private final FieldMap fields;

	SectionReader(Sheet sheet, FieldMap fields) {
		this.sheet = sheet;
		this.fields = fields;
	}

	@Override
	public Cell cellOf(Field field) {
		var row = fields.posOf(field);
		return row.isPresent()
				? Excel.cell(sheet, row.getAsInt(), 1).orElse(null)
				: null;
	}
}
