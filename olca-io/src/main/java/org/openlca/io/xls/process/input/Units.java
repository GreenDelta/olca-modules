package org.openlca.io.xls.process.input;

import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.xls.process.Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Units {

	private final ProcessWorkbook wb;

	private Units(ProcessWorkbook wb) {
		this.wb = wb;
	}

	static void sync(ProcessWorkbook wb) {
		new Units(wb).sync();
	}

	private void sync() {

	}

	private List<Group> syncGroups(Map<String, List<Unit>> units) {
		var sheet = wb.getSheet("Unit groups");
		if (sheet == null)
			return Collections.emptyList();
		var fields = FieldMap.parse(sheet.getRow(0));
	}

	private Map<String, List<Unit>> collectUnits() {
		var sheet = wb.getSheet("Units");
		if (sheet == null)
			return Collections.emptyMap();
		var fields = FieldMap.parse(sheet.getRow(0));
		if (fields.isEmpty())
			return Collections.emptyMap();
		var map = new HashMap<String, List<Unit>>();
		sheet.rowIterator().forEachRemaining(row -> {
			if (row.getRowNum() == 0)
				return;
			var unit = new Unit();
			Util.mapBase(row, fields, unit);
			unit.conversionFactor = fields.num(row, Field.CONVERSION_FACTOR);
			unit.synonyms = fields.str(row, Field.SYNONYMS);
			var group = fields.str(row, Field.UNIT_GROUP);
			var key = EntityIndex.keyOf(group);
			map.computeIfAbsent(key, k -> new ArrayList<>()).add(unit);
		});
		return map;
	}

	private record Group(UnitGroup entity, String defaultProperty) {
	}

}
