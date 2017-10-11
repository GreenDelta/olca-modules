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

	private Config config;
	private Sheet sheet;
	private int row = 0;

	private UnitSheet(Config config) {
		this.config = config;
		sheet = config.workbook.createSheet("Units");
	}

	public static void write(Config config) {
		new UnitSheet(config).write();
	}

	private void write() {
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
		Excel.cell(sheet, row, 0, record.unit.getRefId());
		Excel.cell(sheet, row, 1, record.unit.getName());
		Excel.cell(sheet, row, 2, record.group.getName());
		Excel.cell(sheet, row, 3, record.unit.getDescription());
		Excel.cell(sheet, row, 4, record.unit.getSynonyms());
		Excel.cell(sheet, row, 5, record.unit.getConversionFactor());
		markRefUnit(record);
	}

	private void markRefUnit(Record record) {
		if (!Objects.equals(record.unit, record.group.getReferenceUnit()))
			return;
		for (int i = 0; i < 6; i++)
			Excel.cell(sheet, row, i).setCellStyle(config.headerStyle);
	}

	private List<Record> getRecords() {
		UnitGroupDao dao = new UnitGroupDao(config.database);
		List<Record> records = new ArrayList<>();
		for (UnitGroup group : dao.getAll()) {
			for (Unit unit : group.getUnits()) {
				records.add(new Record(unit, group));
			}
		}
		Collections.sort(records);
		return records;
	}

	private class Record implements Comparable<Record> {

		final Unit unit;
		final UnitGroup group;

		Record(Unit unit, UnitGroup group) {
			this.unit = unit;
			this.group = group;
		}

		@Override
		public int compareTo(Record other) {
			if (Objects.equals(this.group, other.group))
				return Strings.compare(this.unit.getName(),
						other.unit.getName());
			else
				return Strings.compare(this.group.getName(),
						other.group.getName());
		}
	}
}
