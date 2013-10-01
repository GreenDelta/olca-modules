package org.openlca.io.csv.input;

import java.io.InvalidObjectException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.database.SourceDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.io.Categories;
import org.openlca.io.UnitMapping;
import org.openlca.simapro.csv.model.SPInputParameter;
import org.openlca.simapro.csv.model.SPLiteratureReference;
import org.openlca.simapro.csv.model.SPReferenceData;
import org.openlca.simapro.csv.model.SPSubstance;
import org.openlca.simapro.csv.model.SPUnit;

class ReferenceDataImporter {

	private CSVImportCache cache;
	private SPReferenceData referenceData;
	private FlowPropertyDao flowPropertyDao;
	private UnitGroupDao unitGroupDao;
	private RootEntityDao<Unit, BaseDescriptor> unitDao;
	private CategoryDao categoryDao;
	private SourceDao sourceDao;
	private ParameterDao parameterDao;
	IDatabase database;

	ReferenceDataImporter(IDatabase database) {
		cache = new CSVImportCache();
		flowPropertyDao = new FlowPropertyDao(database);
		unitGroupDao = new UnitGroupDao(database);
		unitDao = new RootEntityDao<>(Unit.class, BaseDescriptor.class,
				database);
		categoryDao = new CategoryDao(database);
		sourceDao = new SourceDao(database);
		parameterDao = new ParameterDao(database);
		this.database = database;
	}

	CSVImportCache importData(SPReferenceData referenceData) {
		this.referenceData = referenceData;
		try {
			convertUnitGroups();
			convertUnits();
			convertSubstances();
			convertLiteratureReference();
			System.out.println();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return cache;
	}

	private void convertUnitGroups() throws Exception {
		Map<String, SPUnit> quantities = new HashMap<>();
		Set<String> checkQuantities = new HashSet<>();
		for (SPUnit unit : referenceData.getUnits())
			if (unit.getName().equals(unit.getReferenceUnit()))
				quantities.put(unit.getQuantity(), unit);
		for (SPUnit unit : referenceData.getUnits())
			checkQuantities.add(unit.getQuantity());
		for (String q : checkQuantities)
			if (!quantities.containsKey(q))
				throw new InvalidObjectException(
						"No reference unit for quantity: " + q);

		// TODO: check conversion factor
		for (Map.Entry<String, SPUnit> entry : quantities.entrySet()) {
			UnitGroup unitGroup = unitGroupDao.getForUnit(entry.getValue()
					.getName());
			if (unitGroup == null) {
				unitGroup = new UnitGroup();
				unitGroup.setCategory(Categories.findOrCreateRoot(database,
						ModelType.UNIT_GROUP, "SimaPro unit groups"));
				// unitGroup.setCategory(Utils.findCategory("SimaPro unit groups",
				// ModelType.UNIT_GROUP, categoryDao));
				unitGroup.setName(entry.getKey());
				unitGroup.setRefId(UUID.randomUUID().toString());
				Unit referenceUnit = convert(entry.getValue());
				unitGroup.getUnits().add(referenceUnit);
				unitGroup.setReferenceUnit(referenceUnit);
				unitGroupDao.insert(unitGroup);
				unitGroup
						.setDefaultFlowProperty(createFlowPropertyForUnitGroup(unitGroup));
				unitGroup = unitGroupDao.update(unitGroup);
			}
			cache.unitGroupMap.put(entry.getKey(), unitGroup);
		}
	}

	private FlowProperty createFlowPropertyForUnitGroup(UnitGroup unitGroup)
			throws Exception {
		FlowProperty flowProperty = new FlowProperty();
		flowProperty.setRefId(UUID.randomUUID().toString());
		flowProperty.setName(unitGroup.getName());
		flowProperty.setCategory(Categories.findOrCreateRoot(database,
				ModelType.FLOW_PROPERTY, "SimaPro flow properties"));
		// flowProperty.setCategory(Utils.findCategory("SimaPro flow properties",
		// ModelType.FLOW_PROPERTY, categoryDao));
		flowProperty.setUnitGroup(unitGroup);
		// TODO: check the default type
		flowProperty.setFlowPropertyType(FlowPropertyType.PHYSICAL);
		flowPropertyDao.insert(flowProperty);
		return flowProperty;
	}

	private void convertUnits() throws Exception {
		for (SPUnit spUnit : referenceData.getUnits()) {
			Unit unit = find(spUnit);
			if (unit == null) {
				unit = convert(spUnit);
				UnitGroup unitGroup = cache.unitGroupMap.get(spUnit
						.getQuantity());
				unitGroup.getUnits().add(unit);
				unitGroup = unitGroupDao.update(unitGroup);
			}
			cache.unitMap.put(spUnit.getName(), unit);
		}
	}

	private Unit convert(SPUnit spUnit) {
		if (spUnit == null)
			return null;
		Unit unit = new Unit();
		unit.setName(spUnit.getName());
		unit.setConversionFactor(spUnit.getConversionFactor());
		unit.setRefId(UUID.randomUUID().toString());
		return unit;
	}

	private void convertSubstances() throws Exception {
		for (SPSubstance substance : referenceData.getSubstances()) {
			String key = substance.getName()
					+ substance.getFlowType().getValue();
			cache.substanceMap.put(key, substance);
		}
	}

	private void convertLiteratureReference() throws Exception {
		for (SPLiteratureReference literatureReference : referenceData
				.getLiteratureReferences()) {
			Source source = new Source();
			source.setRefId(UUID.randomUUID().toString());
			source.setName(literatureReference.getName());
			StringBuilder comment = new StringBuilder();
			if (!"".equals(literatureReference.getContent()))
				comment.append(literatureReference.getContent());
			if (!"".equals(literatureReference.getDocumentLink()))
				comment.append("\n" + literatureReference.getDocumentLink());
			if (!comment.toString().equals(""))
				source.setDescription(comment.toString());
			source.setCategory(Categories.findOrCreateRoot(database,
					ModelType.SOURCE, literatureReference.getCategory()));
			// source.setCategory(Utils.findCategory(
			// literatureReference.getCategory(), ModelType.SOURCE,
			// categoryDao));
			boolean insert = true;
			List<Source> list = sourceDao.getForName(literatureReference
					.getName());
			for (Source s : list)
				if (s.getCategory().getId() == source.getCategory().getId()) {
					insert = false;
					break;
				}
			if (insert) {
				sourceDao.insert(source);
			} else {
				// TODO: log not insert
			}
			cache.sourceMap.put(literatureReference.getName()
					+ literatureReference.getCategory(), source);
		}
	}

	private void convertParameters() throws Exception {
		for (SPInputParameter inputParameter : referenceData
				.getInputParameters()) {
			Parameter parameter = Utils.convertInputParameter(inputParameter,
					parameterDao);
			// TODO
		}
	}

	// private FlowProperty getForUnit(String unit) {
	// if (unit == null)
	// return null;
	// UnitGroup unitGroup = unitGroupDao.getForUnit(unit);
	// if (unitGroup == null)
	// return null;
	// return unitGroup.getDefaultFlowProperty();
	// }

	private Unit find(SPUnit spUnit) {
		Unit unit = null;
		List<Unit> list = unitDao.getForName(spUnit.getName());
		if (list == null)
			System.out.println();
		for (Unit u : list) {
			if (u.getConversionFactor() == spUnit.getConversionFactor()) {
				unit = u;
				break;
			}
		}
		return unit;
	}
}
