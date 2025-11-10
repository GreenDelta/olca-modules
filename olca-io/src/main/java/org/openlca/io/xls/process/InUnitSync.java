package org.openlca.io.xls.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openlca.commons.Strings;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

class InUnitSync {

	private final InConfig config;

	private InUnitSync(InConfig config) {
		this.config = config;
	}

	static void sync(InConfig config) {
		new InUnitSync(config).sync();
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
		var sheet = config.getSheet(Tab.UNIT_GROUPS);
		if (sheet == null)
			return Collections.emptyMap();
		var map = new HashMap<String, String>();
		sheet.eachRow(row -> {
			var refId = row.str(Field.UUID);
			config.index().sync(UnitGroup.class, refId, () -> {
				var group = createGroup(row, units);
				var defaultProp = row.str(Field.DEFAULT_FLOW_PROPERTY);
				map.put(group.refId, defaultProp);
				return group;
			});
		});

		return map;
	}

	private UnitGroup createGroup(RowReader row, Map<String, List<Unit>> units) {
		var group = new UnitGroup();
		In.mapBase(row, group);
		group.category = row.syncCategory(config.db(), ModelType.UNIT_GROUP);
		var groupUnits = units.get(EntityIndex.keyOf(group.name));
		if (groupUnits == null)
			return group;
		var refUnit = row.str(Field.REFERENCE_UNIT);
		for (var unit : groupUnits) {
			group.units.add(unit);
			if (Objects.equals(refUnit, unit.name)) {
				group.referenceUnit = unit;
			}
		}
		return group;
	}

	private Map<String, List<Unit>> collectUnits() {
		var sheet = config.getSheet(Tab.UNITS);
		if (sheet == null)
			return Collections.emptyMap();
		var map = new HashMap<String, List<Unit>>();
		sheet.eachRow(row -> {
			var unit = new Unit();
			In.mapBase(row, unit);
			unit.conversionFactor = row.num(Field.CONVERSION_FACTOR);
			unit.synonyms = row.str(Field.SYNONYMS);
			var group = row.str(Field.UNIT_GROUP);
			var key = EntityIndex.keyOf(group);
			map.computeIfAbsent(key, k -> new ArrayList<>()).add(unit);
		});
		return map;
	}

	private void syncProps(Map<String, String> newGroupProps) {
		var sheet = config.getSheet(Tab.FLOW_PROPERTIES);
		if (sheet == null)
			return;
		var props = new ArrayList<FlowProperty>();
		sheet.eachRow(row -> {
			var refId = row.str(Field.UUID);
			var prop = config.index().sync(
				FlowProperty.class, refId, () -> createProp(row));
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
				config.db().update(group);
			}
		}

		// reload properties and unit groups in the index for JPA
		for (var prop : props) {
			var reloaded = config.db().get(FlowProperty.class, prop.id);
			config.index().put(reloaded);
			config.index().put(reloaded.unitGroup);
		}
	}

	private FlowProperty createProp(RowReader row) {
		var prop = new FlowProperty();
		In.mapBase(row, prop);
		prop.category = row.syncCategory(config.db(), ModelType.FLOW_PROPERTY);
		var groupRef = row.str(Field.UNIT_GROUP);
		prop.unitGroup = config.index().get(UnitGroup.class, groupRef);
		prop.flowPropertyType = propTypeOf(row);
		return prop;
	}

	private FlowPropertyType propTypeOf(RowReader row) {
		var propType = row.str(Field.TYPE);
		if (Strings.isBlank(propType))
			return FlowPropertyType.PHYSICAL;
		char c = propType.charAt(0);
		return c == 'E' || c == 'e'
			? FlowPropertyType.ECONOMIC
			: FlowPropertyType.PHYSICAL;
	}
}
