package org.openlca.io.xls.process;

import java.util.EnumMap;
import java.util.OptionalInt;

import org.apache.poi.ss.usermodel.Row;

class FieldMap {

	private final EnumMap<Field, Integer> map = new EnumMap<>(Field.class);

	FieldMap() {
	}

	static FieldMap of(Row row) {
		var fm = new FieldMap();
		if (row == null)
			return fm;
		row.cellIterator().forEachRemaining(cell -> {
			var label = In.stringOf(cell);
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
}
