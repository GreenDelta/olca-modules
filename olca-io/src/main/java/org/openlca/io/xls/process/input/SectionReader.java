package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.io.xls.Excel;
import org.openlca.io.xls.process.Field;

import java.util.Date;

class SectionReader {

	private final Sheet sheet;
	private final FieldMap fields;

	SectionReader(Sheet sheet, FieldMap fields) {
		this.sheet = sheet;
		this.fields = fields;
	}

	String str(Field field) {
		return Excel.getString(cellOf(field));
	}

	Date date(Field field) {
		return Excel.getDate(cellOf(field));
	}

	boolean bool(Field field) {
		return Util.booleanOf(cellOf(field));
	}

	private Cell cellOf(Field field) {
		var pos = fields.posOf(field);
		return pos.isPresent()
				? Excel.cell(sheet, pos.getAsInt(), 1).orElse(null)
				: null;
	}

}
