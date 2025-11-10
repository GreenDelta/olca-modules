package org.openlca.io.xls.process;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

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
				? In.cell(sheet, row.getAsInt(), 1)
				: null;
	}
}
