package org.openlca.io.xls.process;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.commons.Strings;

class SheetReader {

	private final Sheet sheet;

	SheetReader(Sheet sheet) {
		this.sheet = sheet;
	}

	Sheet sheetObject() {
		return sheet;
	}

	/**
	 * Iterates over a block of rows after the given header row. The block is
	 * finished when the first {@code null} row is found; callers can of course
	 * break the iteration earlier.
	 */
	Iterable<Row> eachBlockRowAfter(Row header) {
		return () -> new Iterator<>() {

			int i = header.getRowNum() + 1;

			@Override
			public boolean hasNext() {
				return sheet.getRow(i) != null;
			}

			@Override
			public Row next() {
				var row = sheet.getRow(i);
				i++;
				return row;
			}
		};
	}

	void eachRow(Consumer<RowReader> fn) {
		var fields = FieldMap.of(sheet.getRow(0));
		if (fields.isEmpty())
			return;
		sheet.rowIterator().forEachRemaining(row -> {
			if (row.getRowNum() == 0)
				return;
			fn.accept(RowReader.of(row, fields));
		});
	}

	/**
	 * Reads a field-value section in the sheet.
	 */
	SectionReader read(Section section) {
		var fields = new FieldMap();
		eachRowObject(section, row -> {
			var field = In.stringOf(row, 0);
			fields.put(field, row.getRowNum());
		});
		return new SectionReader(sheet, fields);
	}

	/**
	 * Iterates over each value row under a section.
	 */
	void eachRowObject(Section section, Consumer<Row> fn) {
		if (section == null || fn == null)
			return;
		Row start = null;
		var iter = sheet.rowIterator();
		while (iter.hasNext()) {
			var row = iter.next();
			var head = In.stringOf(row, 0);
			if (section.matches(head)) {
				start = row;
				break;
			}
		}
		if (start == null)
			return;

		int i = start.getRowNum() + 1;
		Row row;
		while ((row = sheet.getRow(i)) != null) {
			var head = In.stringOf(row, 0);
			if (Strings.isBlank(head))
				break;
			fn.accept(row);
			i++;
		}
	}

	void eachRow(Section section, Consumer<RowReader> fn) {
		var fieldsRef = new AtomicReference<FieldMap>();
		eachRowObject(section, row -> {
			var fields = fieldsRef.get();
			if (fields == null) {
				fields = FieldMap.of(row);
				fieldsRef.set(fields);
			} else {
				fn.accept(RowReader.of(row, fields));
			}
		});
	}
}
