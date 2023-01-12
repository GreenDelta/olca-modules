package org.openlca.io.xls.process.input;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.io.xls.Excel;
import org.openlca.io.xls.process.Section;
import org.openlca.util.Strings;

class SheetReader {

	private final Sheet sheet;

	SheetReader(Sheet sheet) {
		this.sheet = sheet;
	}

	void eachRow(BiConsumer<FieldMap, Row> fn) {
		var fields = FieldMap.of(sheet.getRow(0));
		if (fields.isEmpty())
			return;
		sheet.rowIterator().forEachRemaining(row -> {
			if (row.getRowNum() == 0)
				return;
			fn.accept(fields, row);
		});
	}

	/**
	 * Reads a field-value section in the sheet.
	 */
	SectionReader read(Section section) {
		var fields = new FieldMap();
		eachRow(section, row -> {
			var field = Excel.getString(row, 0);
			fields.put(field, row.getRowNum());
		});
		return new SectionReader(sheet, fields);
	}

	/**
	 * Iterates over each value row under a section.
	 */
	void eachRow(Section section, Consumer<Row> fn) {
		if (section == null || fn == null)
			return;
		var iter = sheet.rowIterator();
		boolean inSection = false;
		while (iter.hasNext()) {
			var row = iter.next();
			var firstVal = Excel.getString(row, 0);
			if (!inSection) {
				if (matches(firstVal, section)) {
					inSection = true;
				}
				continue;
			}
			// in section
			if (Strings.nullOrEmpty(firstVal))
				break;
			fn.accept(row);
		}
	}

	void eachRow(Section section, BiConsumer<Row, FieldMap> fn) {
		var fieldsRef = new AtomicReference<FieldMap>();
		eachRow(section, row -> {
			var fields = fieldsRef.get();
			if (fields == null) {
				fields = FieldMap.of(row);
				fieldsRef.set(fields);
			} else {
				fn.accept(row, fields);
			}
		});
	}

	private boolean matches(String fieldId, Section section) {
		if (Strings.nullOrEmpty(fieldId))
			return false;
		return fieldId.trim().equalsIgnoreCase(section.header());
	}

}
