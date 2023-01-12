package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Cell;
import org.openlca.io.xls.Excel;
import org.openlca.io.xls.process.Field;

import java.util.Date;

interface CellReader {

	default String str(Field field) {
		return Excel.getString(cellOf(field));
	}

	default Date date(Field field) {
		return Excel.getDate(cellOf(field));
	}

	default boolean bool(Field field) {
		return Util.booleanOf(cellOf(field));
	}

	Cell cellOf(Field field);

}
