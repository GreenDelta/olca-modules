package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.io.xls.Excel;
import org.openlca.io.xls.process.Field;
import org.openlca.util.Strings;

import java.util.Date;
import java.util.EnumMap;
import java.util.OptionalInt;

class FieldMap {

	private final EnumMap<Field, Integer> map = new EnumMap<>(Field.class);

	FieldMap() {
	}

	static FieldMap of(Row row) {
		var fm = new FieldMap();
		if (row == null)
			return fm;
		row.cellIterator().forEachRemaining(cell -> {
			var label = Excel.getString(cell);
			var field = Field.of(label);
			if (field != null) {
				fm.map.put(field, cell.getColumnIndex());
			}
		});
		return fm;
	}

	boolean isEmpty() {
		return map.isEmpty();
	}

	void put(String fieldId, int pos) {
		var field = Field.of(fieldId);
		if (field != null) {
			map.put(field, pos);
		}
	}

	OptionalInt posOf(Field field) {
		if (field == null)
			return OptionalInt.empty();
		var i = map.get(field);
		return i != null
				? OptionalInt.of(i)
				: OptionalInt.empty();
	}

	String str(Row row, Field field) {
		return Excel.getString(cellOf(row, field));
	}

	Date date(Row row, Field field) {
		return Excel.getDate(cellOf(row, field));
	}

	double num(Row row, Field field) {
		return Excel.getDouble(cellOf(row, field));
	}

	Object value(Row row, Field field) {
		return Excel.getValue(cellOf(row, field));
	}

	Category category(Row row, ModelType type, IDatabase db) {
		var path = str(row, Field.CATEGORY);
		if (Strings.nullOrEmpty(path))
			return null;
		return new CategoryDao(db).sync(type, path.split("/"));
	}

	private Cell cellOf(Row row, Field field) {
		if (row == null || field == null)
			return null;
		var col = map.get(field);
		return col != null
				? row.getCell(col)
				: null;
	}

}