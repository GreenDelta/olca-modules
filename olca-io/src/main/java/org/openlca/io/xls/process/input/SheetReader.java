package org.openlca.io.xls.process.input;

import java.util.function.BiConsumer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

class SheetReader {

	private final Sheet sheet;

	SheetReader(Sheet sheet) {
		this.sheet = sheet;
	}

	void eachRow(BiConsumer<FieldMap, Row> fn) {
		var fields = FieldMap.parse(sheet.getRow(0));
		if (fields.isEmpty())
			return;
		sheet.rowIterator()
				.forEachRemaining(row -> fn.accept(fields, row));
	}

}
