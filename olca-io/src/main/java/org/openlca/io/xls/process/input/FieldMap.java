package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.io.xls.Excel;

import java.util.EnumMap;

class FieldMap {

	private final Sheet sheet;
	private final EnumMap<Field, Integer> map = new EnumMap<>(Field.class);

	private FieldMap(Sheet sheet) {
		this.sheet = sheet;
	}

	String str(Row row, Field field) {
		if (row == null || field == null)
			return null;
		var col = map.get(field);
		if (col == null)
			return null;
		return Excel.getString(row, col);
	}

}
