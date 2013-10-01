package org.openlca.io.csv.input;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.io.Categories;
import org.openlca.io.UnitMapping;
import org.openlca.simapro.csv.model.SPDataEntry;
import org.openlca.simapro.csv.model.SPElementaryFlow;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPSubstance;
import org.openlca.simapro.csv.model.SPWasteTreatment;
import org.openlca.simapro.csv.model.types.ElementaryFlowType;
import org.openlca.simapro.csv.model.types.SubCompartment;

class ProcessImporter {

	private CSVImportCache cash;
	private Process process;
	private SPDataEntry dataEntry;
	private ProcessDao processDao;
	private FlowDao flowDao;
	private LocationDao locationDao;
	private UnitMapping unitMapping;
	private IDatabase database;
	
	/**
	 * key: name + unit + compartment + subcompartment
	 */
	private Map<String, Flow> elementaryFlowMap = new HashMap<>();

	// TODO:move in another class

	public ProcessImporter(IDatabase database) {
		flowDao = new FlowDao(database);
		processDao = new ProcessDao(database);
		locationDao = new LocationDao(database);
		unitMapping = UnitMapping.createDefault(database);
		this.database = database;
	}

	public void resetUnitMapping() {
		unitMapping = UnitMapping.createDefault(database);
	}

	public CSVImportCache getCash() {
		return cash;
	}

	public void setCash(CSVImportCache cash) {
		this.cash = cash;
	}

	public void runImport(SPProcess process) throws Exception {
		this.process = new Process();
		// TODO only for process

		dataEntry = process;
		convertDataEntry();
		processDao.insert(this.process);
	}

	public void runImport(SPWasteTreatment wasteTreatment) throws Exception {
		this.process = new Process();
		// TODO only for waste treatment

		dataEntry = wasteTreatment;
		convertDataEntry();
		processDao.insert(process);
	}

	private void convertDataEntry() throws Exception {
		if (dataEntry != null) {
			setGeneralInformation();
			processType();
			location();
			elementaryExchanges();
		} else {
			// TODO throw exception
		}
	}

	private void setGeneralInformation() {
		ProcessDocumentation documentation = new ProcessDocumentation();
		process.setDocumentation(documentation);
		process.setRefId(UUID.randomUUID().toString());
		process.setName(dataEntry.getDocumentation().getName());
		documentation.setTime(dataEntry.getDocumentation().getTimePeriod()
				.getValue());
		documentation.setTechnology(dataEntry.getDocumentation()
				.getTechnology().getValue());
		process.setInfrastructureProcess(dataEntry.getDocumentation()
				.isInfrastructureProcess());
		if (!"".equals(dataEntry.getDocumentation().getComment()))
			process.setDescription(dataEntry.getDocumentation().getComment());
	}

	private void processType() {
		org.openlca.simapro.csv.model.types.ProcessType processType = dataEntry
				.getDocumentation().getProcessType();
		if (processType != null
				&& processType == org.openlca.simapro.csv.model.types.ProcessType.SYSTEM) {
			process.setProcessType(ProcessType.LCI_RESULT);
		} else {
			process.setProcessType(ProcessType.UNIT_PROCESS);
		}
	}

	private void location() throws Exception {
		String geo = dataEntry.getDocumentation().getGeography().getValue();
		Location location;
		List<Location> list = locationDao.getForName(geo);
		if (list != null && !list.isEmpty()) {
			location = list.get(0);
		} else {
			location = new Location();
			// TODO right refId
			location.setRefId(UUID.randomUUID().toString());
			location.setName(geo);
			location.setDescription("SimaPro location");
			locationDao.insert(location);
		}
		process.setLocation(location);
	}

	private void elementaryExchanges() throws Exception {
		for (SPElementaryFlow elementaryFlow : dataEntry.getElementaryFlows()) {
			if (elementaryFlow.getName().contains("'''''TestFlow"))
				System.out.println();
			Flow flow = get(elementaryFlow);
			Exchange exchange = new Exchange();
			process.getExchanges().add(exchange);
			exchange.setFlow(flow);
			exchange.setAvoidedProduct(false);
			exchange.setUnit(cash.unitMap.get(elementaryFlow.getUnit()));
			if (elementaryFlow.getType() == ElementaryFlowType.RESOURCE)
				exchange.setInput(true);
			else
				exchange.setInput(false);

			// TODO right amount
			// exchange.getResultingAmount().setValue(elementaryFlow.getAmount());
		}
	}

	private Flow get(SPElementaryFlow elementaryFlow) throws Exception {
		Flow flow = null;
		String key = elementaryFlow.getName() + elementaryFlow.getUnit()
				+ elementaryFlow.getType().getValue()
				+ elementaryFlow.getSubCompartment().getValue();
		flow = elementaryFlowMap.get(key);
		if (flow == null)
			flow = find(elementaryFlow);
		if (flow == null) {
			flow = new Flow();
			SPSubstance substance = cash.substanceMap.get(elementaryFlow
					.getName() + elementaryFlow.getType().getValue());
			// TODO right exception
			if (substance == null)
				throw new IllegalAccessError();
			flow.setRefId(UUID.randomUUID().toString());
			flow.setName(substance.getName());
			if (!"".equals(substance.getCASNumber()))
				flow.setCasNumber(substance.getCASNumber());
			flow.setReferenceFlowProperty(unitMapping.getFlowProperty(substance
					.getReferenceUnit()));
			if (!"".equals(substance.getComment())) {
				flow.setDescription(substance.getComment());
				String formula = getFlowFormula(substance.getComment());
				if (!"".equals(formula))
					flow.setFormula(formula);
			}
			flow.setCategory(getElementaryFlowCategory(elementaryFlow));
			flowDao.insert(flow);
			elementaryFlowMap.put(key, flow);
		}
		return flow;
	}

	private String getFlowFormula(String comment) {
		switch (comment.toLowerCase()) {
		case "formula:":
		case " formula:":
		case "\"formula:":
			String formula[] = comment.split("\n");
			if (!"".equals(formula[0]))
				return formula[0].substring(formula[0].indexOf(":") + 1);
		}
		return null;
	}

	private Category getElementaryFlowCategory(SPElementaryFlow elementaryFlow)
			throws Exception {
		String compartment = mapCompartmentCategory(elementaryFlow.getType());
		String subCompartment = mapSubcompartmentCategory(elementaryFlow
				.getSubCompartment());
		Category rootCategory = Categories.findOrCreateRoot(database,
				ModelType.FLOW, "Elementary flows");
		Category parentCategory = Categories.findOrAddChild(database,
				rootCategory, compartment);
		return Categories.findOrAddChild(database, parentCategory,
				subCompartment);
	}

	private String mapSubcompartmentCategory(SubCompartment subCompartment)
			throws Exception {
		String subCategoryName = null;
		switch (subCompartment) {
		case AIRBORNE_HIGH_POP:
			subCategoryName = "high population density";
			break;
		case AIRBORNE_LOW_POP:
			subCategoryName = "low population density";
			break;
		case AIRBORNE_LOW_POP_LONG_TERM:
			subCategoryName = "low population density, long-term";
			break;
		case AIRBORNE_STATOSPHERE_TROPOSHERE:
			subCategoryName = "lower stratosphere + upper troposphere";
			break;
		case WATERBORNE_GROUNDWATER:
			subCategoryName = "ground water";
			break;
		case WATERBORNE_GROUNDWATER_LONG_TERM:
			subCategoryName = "ground water, long-term";
			break;
		default:
			subCategoryName = subCompartment.getValue();
			break;
		}
		return subCategoryName;
		// return Utils.findSubCategory(subCategoryName, compartment.getValue(),
		// ModelType.FLOW, categoryDao);
	}

	// private Category getCompartmentCategory(ElementaryFlowType type)
	// throws Exception {
	// // TODO maybe remove this
	// return Utils.findCategory(mapCompartmentCategory(type),
	// ModelType.CATEGORY, categoryDao);
	// }

	private String mapCompartmentCategory(ElementaryFlowType type) {
		String category = null;
		switch (type) {
		case EMISSION_TO_AIR:
			category = "air";
			break;
		case EMISSION_TO_SOIL:
			category = "soil";
			break;
		case EMISSION_TO_WATER:
			category = "water";
			break;
		case RESOURCE:
			category = "resource";
			break;
		default:
			category = type.getValue();
			break;
		}
		return category;
	}

	private Flow find(SPElementaryFlow elementaryFlow) throws Exception {
		Flow flow = null;
		List<Flow> list = flowDao.getForName(elementaryFlow.getName());
		for (Flow f : list) {
			if (compare(f, elementaryFlow))
				flow = f;
		}
		return flow;
	}

	private boolean compare(Flow flow, SPElementaryFlow elementaryFlow)
			throws Exception {
		SPSubstance substance = cash.substanceMap.get(elementaryFlow.getName()
				+ elementaryFlow.getType().getValue());
		// TODO right exception
		if (substance == null)
			throw new Exception();
		if (!Utils.compareComplete(substance.getCASNumber(),
				flow.getCasNumber()))
			return false;
		if (flow.getReferenceFlowProperty().getId() != unitMapping
				.getFlowProperty(elementaryFlow.getUnit()).getId())
			return false;
		Category category = getElementaryFlowCategory(elementaryFlow);
		if (flow.getCategory().getId() != category.getId())
			return false;
		return true;
	}
}
