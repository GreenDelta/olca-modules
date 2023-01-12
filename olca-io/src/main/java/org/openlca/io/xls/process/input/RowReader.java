package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.openlca.io.xls.Excel;
import org.openlca.io.xls.process.Field;

class RowReader implements CellReader {

	private final Row row;
	private final FieldMap fields;

	private RowReader(Row row, FieldMap fields) {
		this.row = row;
		this.fields = fields;
	}

	static RowReader of(Row row, FieldMap fields) {
		return new RowReader(row, fields);
	}

	@Override
	public Cell cellOf(Field field) {
		var col = fields.posOf(field);
		return col.isPresent()
				? Excel.cell(row, col.getAsInt()).orElse(null)
				: null;
	}

}
