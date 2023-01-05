package org.openlca.io.xls.process.output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.xls.Excel;
import org.openlca.util.Strings;

class UnitSheet implements EntitySheet {

	private final ProcessWorkbook config;
	private final Set<UnitGroup> groups = new HashSet<>();

	UnitSheet(ProcessWorkbook config) {
		this.config = config;
	}

	@Override
	public void visit(RootEntity entity) {
		Util.unitGroupsOf(entity, groups::add);
	}

	@Override
	public void flush() {
		var items = getItems();
		if (items.isEmpty())
			return;

		var sheet = config.workbook.createSheet("Units");
		Excel.trackSize(sheet, 0, 5);
		writeHeader(sheet);
		int row = 0;
		for (var item : getItems()) {
			row++;
			write(sheet, row, item);
		}
		Excel.autoSize(sheet, 0, 5);
	}

	private void writeHeader(Sheet sheet) {
		config.header(sheet, 0, 0, "UUID");
		config.header(sheet, 0, 1, "Name");
		config.header(sheet, 0, 2, "Unit group");
		config.header(sheet, 0, 3, "Description");
		config.header(sheet, 0, 4, "Synonyms");
		config.header(sheet, 0, 5, "Conversion factor");
	}

	private void write(Sheet sheet, int row, Item item) {
		Excel.cell(sheet, row, 0, item.unit.refId);
		Excel.cell(sheet, row, 1, item.unit.name);
		Excel.cell(sheet, row, 2, item.group.name);
		Excel.cell(sheet, row, 3, item.unit.description);
		Excel.cell(sheet, row, 4, item.unit.synonyms);
		Excel.cell(sheet, row, 5, item.unit.conversionFactor);
		if (Objects.equals(item.unit, item.group.referenceUnit)) {
			for (int i = 0; i < 6; i++) {
				Excel.cell(sheet, row, i)
						.ifPresent(c -> c.setCellStyle(config.headerStyle));
			}
		}
	}

	private List<Item> getItems() {
		var items = new ArrayList<Item>();
		for (var group : groups) {
			for (var unit : group.units) {
				items.add(new Item(unit, group));
			}
		}
		Collections.sort(items);
		return items;
	}

	private record Item(Unit unit, UnitGroup group) implements Comparable<Item> {

		@Override
		public int compareTo(Item other) {
			return Objects.equals(group, other.group)
					? Strings.compare(unit.name, other.unit.name)
					: Strings.compare(group.name, other.group.name);
		}
	}
}
