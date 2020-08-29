package org.openlca.io.xls.process.output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.xls.Excel;
import org.openlca.util.Strings;

class UnitSheet {

	private final Config config;
	private final Sheet sheet;
	private int row = 0;

	private UnitSheet(Config config) {
		this.config = config;
		sheet = config.workbook.createSheet("Units");
	}

	public static void write(Config config) {
		new UnitSheet(config).write();
	}

	private void write() {
		Excel.trackSize(sheet, 0, 5);
		writeHeader();
		for (Record record : getRecords()) {
			row++;
			write(record);
		}
		Excel.autoSize(sheet, 0, 5);
	}

	private void writeHeader() {
		config.header(sheet, row, 0, "UUID");
		config.header(sheet, row, 1, "Name");
		config.header(sheet, row, 2, "Unit group");
		config.header(sheet, row, 3, "Description");
		config.header(sheet, row, 4, "Synonyms");
		config.header(sheet, row, 5, "Conversion factor");
	}

	private void write(Record record) {
		Excel.cell(sheet, row, 0, record.unit.refId);
		Excel.cell(sheet, row, 1, record.unit.name);
		Excel.cell(sheet, row, 2, record.group.name);
		Excel.cell(sheet, row, 3, record.unit.description);
		Excel.cell(sheet, row, 4, record.unit.synonyms);
		Excel.cell(sheet, row, 5, record.unit.conversionFactor);
		markRefUnit(record);
	}

	private void markRefUnit(Record record) {
		if (!Objects.equals(record.unit, record.group.referenceUnit))
			return;
		for (int i = 0; i < 6; i++) {
			Excel.cell(sheet, row, i)
					.ifPresent(c -> c.setCellStyle(config.headerStyle));
		}
	}

	private List<Record> getRecords() {
		UnitGroupDao dao = new UnitGroupDao(config.database);
		List<Record> records = new ArrayList<>();
		for (UnitGroup group : dao.getAll()) {
			for (Unit unit : group.units) {
				records.add(new Record(unit, group));
			}
		}
		Collections.sort(records);
		return records;
	}

	private static class Record implements Comparable<Record> {

		final Unit unit;
		final UnitGroup group;

		Record(Unit unit, UnitGroup group) {
			this.unit = unit;
			this.group = group;
		}

		@Override
		public int compareTo(Record other) {
			if (Objects.equals(this.group, other.group))
				return Strings.compare(this.unit.name,
						other.unit.name);
			else
				return Strings.compare(this.group.name,
						other.group.name);
		}
	}
}
