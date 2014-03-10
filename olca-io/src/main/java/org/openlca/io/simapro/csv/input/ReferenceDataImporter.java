package org.openlca.io.simapro.csv.input;

import java.io.InvalidObjectException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.InterpreterException;
import org.openlca.io.Categories;
import org.openlca.io.KeyGen;
import org.openlca.io.UnitMapping;
import org.openlca.io.UnitMappingEntry;
import org.openlca.io.maps.ImportMap;
import org.openlca.io.maps.MapType;
import org.openlca.io.maps.MappingBuilder;
import org.openlca.io.maps.content.CSVUnitContent;
import org.openlca.simapro.csv.model.CalculatedParameterRow;
import org.openlca.simapro.csv.model.InputParameterRow;
import org.openlca.simapro.csv.model.SPReferenceData;
import org.openlca.simapro.csv.model.refdata.ElementaryFlowRow;
import org.openlca.simapro.csv.model.refdata.UnitRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ReferenceDataImporter {

	private Logger log = LoggerFactory.getLogger(getClass());
	private CSVImportCache cache;
	private SPReferenceData referenceData;
	private FlowPropertyDao flowPropertyDao;
	private UnitGroupDao unitGroupDao;
	private RootEntityDao<Unit, BaseDescriptor> unitDao;
	private ParameterDao parameterDao;
	private FormulaInterpreter interpreter;
	private IDatabase database;
	private UnitMapping unitMapping;
	private ImportMap<CSVUnitContent> importMap;

	ReferenceDataImporter(IDatabase database, FormulaInterpreter interpreter) {
		cache = new CSVImportCache();
		flowPropertyDao = new FlowPropertyDao(database);
		unitGroupDao = new UnitGroupDao(database);
		unitDao = new RootEntityDao<>(Unit.class, BaseDescriptor.class,
				database);
		parameterDao = new ParameterDao(database);
		this.database = database;
		this.interpreter = interpreter;
		this.unitMapping = UnitMapping.createDefault(database);
		MappingBuilder mappingBuilder = new MappingBuilder(database);
		importMap = mappingBuilder.buildImportMapping(CSVUnitContent.class,
				MapType.CSV_UNIT);
	}

	CSVImportCache importData(SPReferenceData referenceData)
			throws InvalidObjectException, InterpreterException {
		this.referenceData = referenceData;
		convertUnitGroups();
		convertUnits();
		convertSubstances();
		convertParameters();
		calculateParameterValues();
		return cache;
	}

	private void convertUnitGroups() throws InvalidObjectException {
		Map<String, UnitRow> quantities = new HashMap<>();
		Set<String> checkQuantities = new HashSet<>();
		for (UnitRow unit : referenceData.getUnits().values())
			if (unit.getName().equals(unit.getReferenceUnit()))
				quantities.put(unit.getQuantity(), unit);
		for (UnitRow unit : referenceData.getUnits().values())
			checkQuantities.add(unit.getQuantity());
		for (String q : checkQuantities)
			if (!quantities.containsKey(q))
				throw new InvalidObjectException(
						"No reference unit for quantity: " + q);

		// TODO: check conversion factor
		for (Map.Entry<String, UnitRow> entry : quantities.entrySet()) {
			UnitMappingEntry unitMappingEntry = unitMapping.getEntry(entry
					.getValue().getName());
			UnitGroup unitGroup = null;
			if (unitMappingEntry != null)
				unitGroup = unitMappingEntry.getUnitGroup();
			if (unitGroup == null) {
				unitGroup = new UnitGroup();
				unitGroup.setCategory(Categories.findOrCreateRoot(database,
						ModelType.UNIT_GROUP, "SimaPro unit groups"));
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

	private FlowProperty createFlowPropertyForUnitGroup(UnitGroup unitGroup) {
		FlowProperty flowProperty = new FlowProperty();
		flowProperty.setRefId(UUID.randomUUID().toString());
		flowProperty.setName(unitGroup.getName());
		flowProperty.setCategory(Categories.findOrCreateRoot(database,
				ModelType.FLOW_PROPERTY, "SimaPro flow properties"));
		flowProperty.setUnitGroup(unitGroup);
		// TODO: check the default type
		flowProperty.setFlowPropertyType(FlowPropertyType.PHYSICAL);
		flowPropertyDao.insert(flowProperty);
		return flowProperty;
	}

	private void convertUnits() {
		for (UnitRow spUnit : referenceData.getUnits().values()) {
			Unit unit = find(spUnit);
			if (unit == null) {
				unit = convert(spUnit);
				UnitGroup unitGroup = cache.unitGroupMap.get(spUnit
						.getQuantity());
				unitGroup.getUnits().add(unit);
				unitGroup = unitGroupDao.update(unitGroup);
			}
		}
	}

	private Unit convert(UnitRow spUnit) {
		if (spUnit == null)
			return null;
		Unit unit = new Unit();
		unit.setName(spUnit.getName());
		unit.setConversionFactor(spUnit.getConversionFactor());
		unit.setRefId(KeyGen.get(spUnit.getName()));
		return unit;
	}

	private void convertSubstances() {
		for (ElementaryFlowRow substance : referenceData.getSubstances().values()) {
			String key = substance.getName()
					+ substance.getFlowType().getValue();
			cache.substanceMap.put(key, substance);
		}
	}

	private void convertParameters() {
		for (InputParameterRow parameter : referenceData.getInputParameters()
				.values()) {
			if (!containsParameter(parameter.getName()))
				parameterDao.insert(Utils.create(parameter,
						ParameterScope.GLOBAL));
			interpreter.getGlobalScope().bind(parameter.getName(),
					String.valueOf(parameter.getValue()));
		}
		for (CalculatedParameterRow parameter : referenceData
				.getCalculatedParameters().values()) {
			if (!containsParameter(parameter.getName()))
				parameterDao.insert(Utils.create(parameter,
						ParameterScope.GLOBAL));
			interpreter.getGlobalScope().bind(parameter.getName(),
					parameter.getExpression());
		}
	}

	private boolean containsParameter(String name) {
		List<Parameter> parameters = parameterDao.getGlobalParameters();
		if (parameters.contains(name)) {
			log.debug("Parameter will not be added because it already exists: "
					+ name);
			return true;
		}
		return false;
	}

	private void calculateParameterValues() throws InterpreterException {
		for (Parameter parameter : parameterDao.getGlobalParameters())
			if (!parameter.isInputParameter()) {
				parameter.setValue(interpreter.eval(parameter.getFormula()));
				parameterDao.update(parameter);
			}
	}

	private Unit find(UnitRow spUnit) {

		Unit unit = unitDao.getForRefId(importMap.getOlcaId(CSVKeyGen
				.forUnit(spUnit)));
		if (unit != null)
			return unit;

		List<Unit> list = unitDao.getForName(spUnit.getName());
		for (Unit u : list) {
			// TODO: check
			return u;
		}
		return null;
	}
}
