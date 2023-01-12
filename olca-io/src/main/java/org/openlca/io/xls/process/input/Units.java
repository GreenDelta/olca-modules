package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Row;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.xls.process.Field;
import org.openlca.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class Units {

	private final ProcessWorkbook wb;

	private Units(ProcessWorkbook wb) {
		this.wb = wb;
	}

	static void sync(ProcessWorkbook wb) {
		new Units(wb).sync();
	}

	private void sync() {
		var units = collectUnits();
		var newGroupProps = syncGroups(units);
		syncProps(newGroupProps);
	}

	/**
	 * Synchronizes the unit groups of the workbook with the database. It returns
	 * the default flow property relations as map for the unit groups that were
	 * created during the synchronization. These new unit groups are linked then
	 * to the respective flow properties when these flow properties are
	 * synchronized.
	 */
	private Map<String, String> syncGroups(Map<String, List<Unit>> units) {
		var sheet = wb.getSheet("Unit groups");
		if (sheet == null)
			return Collections.emptyMap();
		var fields = FieldMap.of(sheet.getRow(0));
		if (fields.isEmpty())
			return Collections.emptyMap();

		var map = new HashMap<String, String>();
		sheet.rowIterator().forEachRemaining(row -> {
			if (row.getRowNum() == 0)
				return;
			var refId = fields.str(row, Field.UUID);
			wb.index.sync(UnitGroup.class, refId, () -> {
				var group = createGroup(row, fields, units);
				var defaultProp = fields.str(row, Field.DEFAULT_FLOW_PROPERTY);
				map.put(group.refId, defaultProp);
				return group;
			});
		});
		return map;
	}

	private UnitGroup createGroup(
			Row row, FieldMap fields, Map<String, List<Unit>> units) {
		var group = new UnitGroup();
		Util.mapBase(row, fields, group);
		group.category = fields.category(row, ModelType.UNIT_GROUP, wb.db);
		var groupUnits = units.get(EntityIndex.keyOf(group.name));
		if (groupUnits == null)
			return group;
		var refUnit = fields.str(row, Field.REFERENCE_UNIT);
		for (var unit : groupUnits) {
			group.units.add(unit);
			if (Objects.equals(refUnit, unit.name)) {
				group.referenceUnit = unit;
			}
		}
		return group;
	}

	private Map<String, List<Unit>> collectUnits() {
		var sheet = wb.getSheet("Units");
		if (sheet == null)
			return Collections.emptyMap();
		var fields = FieldMap.of(sheet.getRow(0));
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

	private void syncProps(Map<String, String> newGroupProps) {
		var sheet = wb.getSheet("Flow properties");
		if (sheet == null)
			return;
		var fields = FieldMap.of(sheet.getRow(0));
		if (fields.isEmpty())
			return;

		var props = new ArrayList<FlowProperty>();
		sheet.rowIterator().forEachRemaining(row -> {
			if (row.getRowNum() == 0)
				return;
			var refId = fields.str(row, Field.UUID);
			var prop = wb.index.sync(
					FlowProperty.class, refId, () -> createProp(row, fields));
			if (prop != null) {
				props.add(prop);
			}
		});

		// set default properties
		if (newGroupProps.isEmpty())
			return;
		for (var prop : props) {
			var group = prop.unitGroup;
			if (group == null)
				continue;
			var defaultProp = newGroupProps.get(group.refId);
			if (Objects.equals(defaultProp, prop.name)) {
				group.defaultFlowProperty = prop;
				wb.db.update(group);
			}
		}

		// reload properties and unit groups in the index for JPA
		for (var prop : props) {
			var reloaded = wb.db.get(FlowProperty.class, prop.id);
			wb.index.put(reloaded);
			wb.index.put(reloaded.unitGroup);
		}
	}

	private FlowProperty createProp(Row row, FieldMap fields) {
		var prop = new FlowProperty();
		Util.mapBase(row, fields, prop);
		prop.category = fields.category(row, ModelType.FLOW_PROPERTY, wb.db);
		var groupRef = fields.str(row, Field.UNIT_GROUP);
		prop.unitGroup = wb.index.get(UnitGroup.class, groupRef);
		prop.flowPropertyType = propTypeOf(row, fields);
		return prop;
	}

	private FlowPropertyType propTypeOf(Row row, FieldMap fields) {
		var propType = fields.str(row, Field.TYPE);
		if (Strings.nullOrEmpty(propType))
			return FlowPropertyType.PHYSICAL;
		char c = propType.charAt(0);
		return c == 'E' || c == 'e'
				? FlowPropertyType.ECONOMIC
				: FlowPropertyType.PHYSICAL;
	}
}
