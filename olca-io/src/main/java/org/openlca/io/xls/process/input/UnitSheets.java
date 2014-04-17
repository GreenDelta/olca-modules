package org.openlca.io.xls.process.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronizes units, unit groups, and flow properties with the database.
 */
class UnitSheets {

	private class PropertyRecord extends Record {
		String category;
		Date lastChange;
		String type;
		String unitGroup;
		String version;

		@Override
		void fill(final int row, final Sheet sheet) {
			name = config.getString(sheet, row, 1);
			description = config.getString(sheet, row, 2);
			category = config.getString(sheet, row, 3);
			unitGroup = config.getString(sheet, row, 4);
			type = config.getString(sheet, row, 5);
			version = config.getString(sheet, row, 6);
			lastChange = config.getDate(sheet, row, 7);
		}
	}

	private abstract class Record {
		String description;
		String name;
		String uuid;

		abstract void fill(int row, Sheet sheet);
	}

	private class UnitGroupRecord extends Record {
		String category;
		String defaultProperty;
		Date lastChange;
		String refUnit;
		String version;

		@Override
		void fill(final int row, final Sheet sheet) {
			name = config.getString(sheet, row, 1);
			description = config.getString(sheet, row, 2);
			category = config.getString(sheet, row, 3);
			refUnit = config.getString(sheet, row, 4);
			defaultProperty = config.getString(sheet, row, 5);
			version = config.getString(sheet, row, 6);
			lastChange = config.getDate(sheet, row, 7);
		}
	}

	private class UnitRecord extends Record {
		double conversionFactor;
		String synonyms;
		String unitGroup;

		@Override
		void fill(final int row, final Sheet sheet) {
			name = config.getString(sheet, row, 1);
			unitGroup = config.getString(sheet, row, 2);
			description = config.getString(sheet, row, 3);
			synonyms = config.getString(sheet, row, 4);
			conversionFactor = config.getDouble(sheet, row, 5);
		}
	}

	public static void read(final Config config) {
		new UnitSheets(config).read();
	}

	private final Config config;
	private final List<Pair<PropertyRecord, FlowProperty>> createdProperties = new ArrayList<>();

	private final List<Pair<UnitGroupRecord, UnitGroup>> createdUnitGroups = new ArrayList<>();
	private final UnitGroupDao groupDao;
	private List<UnitGroupRecord> groupRecords;
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final FlowPropertyDao propertyDao;

	private List<PropertyRecord> propertyRecords;

	private final List<FlowProperty> syncedProperties = new ArrayList<>();

	private final List<UnitGroup> syncedUnitGroups = new ArrayList<>();

	private List<UnitRecord> unitRecords;

	private UnitSheets(final Config config) {
		this.config = config;
		this.groupDao = new UnitGroupDao(config.database);
		this.propertyDao = new FlowPropertyDao(config.database);
	}

	private FlowProperty createProperty(final PropertyRecord record) {
		final FlowProperty property = new FlowProperty();
		property.setRefId(record.uuid);
		property.setName(record.name);
		property.setDescription(record.description);
		property.setCategory(config.getCategory(record.category,
				ModelType.FLOW_PROPERTY));
		if (Objects.equals(record.type, "Economic")) {
			property.setFlowPropertyType(FlowPropertyType.ECONOMIC);
		} else {
			property.setFlowPropertyType(FlowPropertyType.PHYSICAL);
		}
		property.setVersion(Version.fromString(record.version).getValue());
		if (record.lastChange != null) {
			property.setLastChange(record.lastChange.getTime());
		}
		propertyDao.insert(property);
		return property;
	}

	private UnitGroup createUnitGroup(final UnitGroupRecord record) {
		final UnitGroup group = new UnitGroup();
		final HashMap<String, Unit> units = getUnits(record.name);
		group.getUnits().addAll(units.values());
		group.setRefId(record.uuid);
		group.setName(record.name);
		group.setDescription(record.description);
		group.setCategory(config.getCategory(record.category,
				ModelType.UNIT_GROUP));
		group.setReferenceUnit(units.get(record.refUnit));
		group.setVersion(Version.fromString(record.version).getValue());
		if (record.lastChange != null) {
			group.setLastChange(record.lastChange.getTime());
		}
		groupDao.insert(group);
		return group;
	}

	private FlowProperty getFlowProperty(final String name) {
		if (name == null) {
			return null;
		}
		for (final FlowProperty property : syncedProperties) {
			if (Objects.equals(name, property.getName())) {
				return property;
			}
		}
		for (final Pair<PropertyRecord, FlowProperty> pair : createdProperties) {
			if (Objects.equals(name, pair.getLeft().name)) {
				return pair.getRight();
			}
		}
		return null;
	}

	private UnitGroup getUnitGroup(final String name) {
		if (name == null) {
			return null;
		}
		for (final UnitGroup group : syncedUnitGroups) {
			if (Objects.equals(name, group.getName())) {
				return group;
			}
		}
		for (final Pair<UnitGroupRecord, UnitGroup> pair : createdUnitGroups) {
			if (Objects.equals(name, pair.getLeft().name)) {
				return pair.getRight();
			}
		}
		return null;
	}

	private HashMap<String, Unit> getUnits(final String unitGroup) {
		final HashMap<String, Unit> units = new HashMap<>();
		for (final UnitRecord record : unitRecords) {
			if (!Objects.equals(record.unitGroup, unitGroup)) {
				continue;
			}
			final Unit unit = new Unit();
			unit.setConversionFactor(record.conversionFactor);
			unit.setDescription(record.description);
			unit.setRefId(record.uuid);
			unit.setName(record.name);
			unit.setSynonyms(record.synonyms);
			units.put(record.name, unit);
		}
		return units;
	}

	private void importFlowProperties() {
		for (final PropertyRecord propertyRecord : propertyRecords) {
			FlowProperty property = propertyDao
					.getForRefId(propertyRecord.uuid);
			if (property == null) {
				property = createProperty(propertyRecord);
				createdProperties.add(Pair.of(propertyRecord, property));
			} else {
				syncedProperties.add(property);
			}
		}
	}

	private void importUnitGroups() {
		for (final UnitGroupRecord groupRecord : groupRecords) {
			UnitGroup group = groupDao.getForRefId(groupRecord.uuid);
			if (group == null) {
				group = createUnitGroup(groupRecord);
				createdUnitGroups.add(Pair.of(groupRecord, group));
			} else {
				group = syncUnitGroup(group, groupRecord);
				syncedUnitGroups.add(group);
			}
		}
	}

	private void linkFlowProperties() {
		for (final Pair<PropertyRecord, FlowProperty> pair : createdProperties) {
			final PropertyRecord record = pair.getLeft();
			final FlowProperty property = pair.getRight();
			final UnitGroup group = getUnitGroup(record.unitGroup);
			if (group == null) {
				log.error("no unit group {} found for property {}",
						record.unitGroup, property);
			}
			property.setUnitGroup(group);
			syncedProperties.add(propertyDao.update(property));
		}
	}

	private void linkUnitGroups() {
		for (final Pair<UnitGroupRecord, UnitGroup> pair : createdUnitGroups) {
			final UnitGroupRecord record = pair.getLeft();
			final UnitGroup group = pair.getRight();
			final FlowProperty property = getFlowProperty(record.defaultProperty);
			if (property == null) {
				continue;
			}
			group.setDefaultFlowProperty(property);
			syncedUnitGroups.add(groupDao.update(group));
		}
	}

	private void read() {
		try {
			log.trace("import units and flow properties");
			final Sheet unitSheet = config.workbook.getSheet("Units");
			unitRecords = readRecords(UnitRecord.class, unitSheet);
			final Sheet groupSheet = config.workbook.getSheet("Unit groups");
			groupRecords = readRecords(UnitGroupRecord.class, groupSheet);
			final Sheet propertySheet = config.workbook
					.getSheet("Flow properties");
			propertyRecords = readRecords(PropertyRecord.class, propertySheet);
			importUnitGroups();
			importFlowProperties();
			linkFlowProperties();
			linkUnitGroups();
			config.refData.loadUnits(config.database);
		} catch (final Exception e) {
			log.error("failed to read unit records", e);
		}
	}

	private <T extends Record> List<T> readRecords(final Class<T> clazz,
			final Sheet sheet) throws Exception {
		if (sheet == null) {
			return Collections.emptyList();
		}
		final List<T> records = new ArrayList<>();
		int row = 1;
		while (true) {
			final String uuid = config.getString(sheet, row, 0);
			if (uuid == null || uuid.trim().isEmpty()) {
				break;
			}
			// non-static private class constructors have an implicit argument
			// with the type of the outer class
			final T record = clazz.getConstructor(getClass()).newInstance(this);
			record.uuid = uuid;
			record.fill(row, sheet);
			records.add(record);
			row++;
		}
		return records;
	}

	private UnitGroup syncUnitGroup(final UnitGroup unitGroup,
			final UnitGroupRecord record) {
		final HashMap<String, Unit> sheetUnits = getUnits(record.name);
		final Unit refUnit = unitGroup.getReferenceUnit();
		final boolean canAdd = refUnit != null
				&& Objects.equals(refUnit.getName(), record.refUnit);
		boolean updated = false;
		for (final Unit sheetUnit : sheetUnits.values()) {
			final Unit realUnit = unitGroup.getUnit(sheetUnit.getName());
			if (realUnit != null) {
				continue;
			}
			if (!canAdd) {
				log.error("unit {} not exists in unit group {} but cannot be"
						+ "added as the reference unit is different to the "
						+ "reference unit in the Excel file", sheetUnit,
						unitGroup);
				continue;
			}
			unitGroup.getUnits().add(sheetUnit);
			updated = true;
		}
		return updated ? groupDao.update(unitGroup) : unitGroup;
	}
}
