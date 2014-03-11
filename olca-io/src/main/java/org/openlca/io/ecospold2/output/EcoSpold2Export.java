package org.openlca.io.ecospold2.output;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.ecospold2.Activity;
import org.openlca.ecospold2.AdministrativeInformation;
import org.openlca.ecospold2.Classification;
import org.openlca.ecospold2.Compartment;
import org.openlca.ecospold2.DataEntryBy;
import org.openlca.ecospold2.DataGenerator;
import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.EcoSpold2;
import org.openlca.ecospold2.ElementaryExchange;
import org.openlca.ecospold2.FileAttributes;
import org.openlca.ecospold2.Geography;
import org.openlca.ecospold2.IntermediateExchange;
import org.openlca.ecospold2.MacroEconomicScenario;
import org.openlca.ecospold2.Representativeness;
import org.openlca.ecospold2.Technology;
import org.openlca.ecospold2.TimePeriod;
import org.openlca.io.ecospold2.UncertaintyConverter;
import org.slf4j.Logger;

import com.google.common.base.Joiner;

/**
 * Exports a set of processes to the EcoSpold 2 data format to a directory. The
 * process data sets are converted to EcoSpold 2 activity data sets and written
 * to the sub-folder 'Activities' in a given export directory.
 */
public class EcoSpold2Export implements Runnable {

	private Logger log;
	private File dir;
	private IDatabase database;
	private List<ProcessDescriptor> descriptors;

	public EcoSpold2Export(File dir, IDatabase database,
			List<ProcessDescriptor> descriptors) {
		this.dir = dir;
		this.database = database;
		this.descriptors = descriptors;
	}

	@Override
	public void run() {
		try {
			File activityDir = new File(dir, "Activities");
			if (!activityDir.exists())
				activityDir.mkdirs();
			exportProcesses(activityDir);
		} catch (Exception e) {
			log.error("EcoSpold 2 export failed", e);
		}
	}

	private void exportProcesses(File activityDir) throws Exception {
		for (ProcessDescriptor descriptor : descriptors) {
			ProcessDao dao = new ProcessDao(database);
			Process process = dao.getForId(descriptor.getId());
			ProcessDocumentation doc = process.getDocumentation();
			if (process == null || doc == null) {
				log.warn("no process entity or documentation for {} found",
						descriptor);
				continue;
			}
			DataSet dataSet = new DataSet();
			Activity activity = createActivity(process);
			dataSet.setActivity(activity);
			// TODO:
			// if (process.getCategory() != null)
			// dataSet.getClassifications().add(
			// convertCategory(process.getCategory()));
			mapGeography(process, dataSet);
			addEconomicScenario(dataSet);
			mapTechnology(doc, dataSet);
			mapTime(doc, dataSet);
			mapRepresentativeness(doc, dataSet);
			mapExchanges(process, dataSet);
			mapParameters(process, dataSet);
			mapAdminInfo(doc, dataSet);
			// TODO add a check box if want merge or not
			mergeElemExchanges(dataSet);
			mergeTechExchanges(dataSet);
			MasterData.map(process, dataSet);
			// if (setRequiredFields)
			// requiredFields.check(dataSet);
			String fileName = process.getRefId() == null ? UUID.randomUUID()
					.toString() : process.getRefId();
			File file = new File(activityDir, fileName + ".spold");
			EcoSpold2.writeDataSet(dataSet, file);
		}
	}

	private Activity createActivity(Process process) {
		Activity activity = new Activity();
		activity.setName(process.getName());
		activity.setId(process.getRefId());
		activity.setActivityNameId(UUID.randomUUID().toString());
		int type = process.getProcessType() == ProcessType.LCI_RESULT ? 2 : 1;
		activity.setType(type);
		activity.setSpecialActivityType(0); // default
		activity.setGeneralComment(process.getDescription());
		return activity;
	}

	private void mergeTechExchanges(DataSet dataSet) {
		Map<String, List<IntermediateExchange>> map = new HashMap<>();
		for (IntermediateExchange exchange : dataSet.getIntermediateExchanges()) {
			if (map.containsKey(exchange.getIntermediateExchangeId())) {
				List<IntermediateExchange> list = map.get(exchange
						.getIntermediateExchangeId());
				list.add(exchange);
			} else {
				List<IntermediateExchange> list = new ArrayList<>();
				list.add(exchange);
				map.put(exchange.getIntermediateExchangeId(), list);
			}
		}
		List<IntermediateExchange> newExchanges = new ArrayList<>();
		for (List<IntermediateExchange> list : map.values()) {
			if (list.size() > 1) {
				double amount = 0;
				for (IntermediateExchange e : list)
					amount += e.getAmount();
				IntermediateExchange exchange = list.get(0);
				exchange.setAmount(amount);
				newExchanges.add(exchange);
			} else {
				if (!list.isEmpty())
					newExchanges.add(list.get(0));
			}
		}
		dataSet.getIntermediateExchanges().clear();
		dataSet.getIntermediateExchanges().addAll(newExchanges);
	}

	private void mergeElemExchanges(DataSet dataSet) {
		Map<String, List<ElementaryExchange>> map = new HashMap<>();
		for (ElementaryExchange exchange : dataSet.getElementaryExchanges()) {
			if (map.containsKey(exchange.getElementaryExchangeId())) {
				List<ElementaryExchange> list = map.get(exchange
						.getElementaryExchangeId());
				list.add(exchange);
			} else {
				List<ElementaryExchange> list = new ArrayList<>();
				list.add(exchange);
				map.put(exchange.getElementaryExchangeId(), list);
			}
		}
		List<ElementaryExchange> newExchanges = new ArrayList<>();
		for (List<ElementaryExchange> list : map.values()) {
			if (list.size() > 1) {
				double amount = 0;
				for (ElementaryExchange e : list)
					amount += e.getAmount();
				ElementaryExchange exchange = list.get(0);
				exchange.setAmount(amount);
				newExchanges.add(exchange);
			} else {
				if (!list.isEmpty())
					newExchanges.add(list.get(0));
			}
		}
		dataSet.getElementaryExchanges().clear();
		dataSet.getElementaryExchanges().addAll(newExchanges);
	}

	// TODO: We can use only the classifications from the master data
	private Classification convertCategory(Category category) {
		if (category == null)
			return null;
		Classification classification = new Classification();
		classification.setClassificationId(category.getRefId());
		classification.setClassificationSystem("openLCA");
		List<String> path = new ArrayList<>();
		Category c = category;
		while (c != null) {
			path.add(0, c.getName());
			c = c.getParentCategory();
		}
		classification.setClassificationValue(Joiner.on('/').skipNulls()
				.join(path));
		return classification;
	}

	private void mapGeography(Process process, DataSet dataSet) {
		Geography geography = new Geography();
		if (process.getDocumentation() != null)
			geography.setComment(process.getDocumentation().getGeography());
		if (process.getLocation() != null) {
			Location location = process.getLocation();
			geography.setId(location.getRefId());
			geography.setShortName(location.getCode());
		}
		// TODO: integrate geography mapping
		if (geography.getId() == null || geography.getShortName() == null) {
			geography.setId("34dbbff8-88ce-11de-ad60-0019e336be3a");
			geography.setShortName("GLO");
		}
		dataSet.setGeography(geography);
	}

	private void mapTechnology(ProcessDocumentation doc, DataSet dataSet) {
		Technology technology = new Technology();
		technology.setComment(doc.getTechnology());
		technology.setTechnologyLevel(0);
		dataSet.setTechnology(technology);
	}

	private void mapTime(ProcessDocumentation doc, DataSet dataSet) {
		TimePeriod timePeriod = new TimePeriod();
		timePeriod.setComment(doc.getTime());
		timePeriod.setDataValid(true);
		if (doc.getValidUntil() != null)
			timePeriod.setEndDate(doc.getValidUntil());
		else
			timePeriod.setEndDate(new Date());
		if (doc.getValidFrom() != null)
			timePeriod.setStartDate(doc.getValidFrom());
		else
			timePeriod.setStartDate(new Date());
		dataSet.setTimePeriod(timePeriod);
	}

	private void addEconomicScenario(DataSet dataSet) {
		MacroEconomicScenario scenario = new MacroEconomicScenario();
		scenario.setId("d9f57f0a-a01f-42eb-a57b-8f18d6635801");
		scenario.setName("Business-as-Usual");
		dataSet.setMacroEconomicScenario(scenario);
	}

	private void mapExchanges(Process process, DataSet dataSet) {
		for (Exchange exchange : process.getExchanges()) {
			if (!isValid(exchange))
				continue;
			org.openlca.ecospold2.Exchange e2Exchange = null;
			Flow flow = exchange.getFlow();
			if (flow.getFlowType() == FlowType.ELEMENTARY_FLOW) {
				e2Exchange = createElementaryExchange(exchange);
				dataSet.getElementaryExchanges().add(
						(ElementaryExchange) e2Exchange);
			} else {
				e2Exchange = createIntermediateExchange(exchange, process);
				dataSet.getIntermediateExchanges().add(
						(IntermediateExchange) e2Exchange);
			}
			mapExchange(exchange, e2Exchange);
		}
	}

	private boolean isValid(Exchange exchange) {
		return exchange.getFlow() != null
				&& exchange.getFlowPropertyFactor() != null
				&& exchange.getUnit() != null;
	}

	private org.openlca.ecospold2.Exchange createElementaryExchange(
			Exchange exchange) {
		ElementaryExchange e2Ex = new ElementaryExchange();
		if (exchange.isInput())
			e2Ex.setInputGroup(4);
		else
			e2Ex.setOutputGroup(4);
		Flow flow = exchange.getFlow();
		e2Ex.setElementaryExchangeId(flow.getRefId());
		if (flow.getCategory() != null) {
			Compartment compartment = convertCompartment(flow.getCategory());
			e2Ex.setCompartment(compartment);
		}
		e2Ex.setFormula(flow.getFormula());
		return e2Ex;
	}

	private Compartment convertCompartment(Category category) {
		Compartment compartment = new Compartment();
		compartment.setSubcompartmentId(category.getRefId());
		compartment.setSubcompartment(category.getName());
		if (category.getParentCategory() != null)
			compartment.setCompartment(category.getParentCategory().getName());
		return compartment;
	}

	private org.openlca.ecospold2.Exchange createIntermediateExchange(
			Exchange exchange, Process process) {
		IntermediateExchange e2Ex = new IntermediateExchange();
		if (exchange.isInput())
			e2Ex.setInputGroup(5);
		else {
			if (Objects.equals(exchange, process.getQuantitativeReference()))
				e2Ex.setOutputGroup(0);
			else if (exchange.getFlow().getFlowType() == FlowType.WASTE_FLOW)
				e2Ex.setOutputGroup(3);
			else
				e2Ex.setOutputGroup(2);
		}
		e2Ex.setIntermediateExchangeId(exchange.getFlow().getRefId());
		ProcessDescriptor provider = getDefaultProvider(exchange);
		if (provider != null)
			e2Ex.setActivityLinkId(provider.getRefId());
		// TODO: We can use only the classifications from the master data
		// if (exchange.getFlow().getCategory() != null)
		// e2Ex.getClassifications().add(
		// convertCategory(exchange.getFlow().getCategory()));
		return e2Ex;
	}

	private ProcessDescriptor getDefaultProvider(Exchange exchange) {
		if (!exchange.isInput() || exchange.getDefaultProviderId() == 0)
			return null;
		ProcessDao dao = new ProcessDao(database);
		return dao.getDescriptor(exchange.getDefaultProviderId());
	}

	private void mapExchange(Exchange exchange,
			org.openlca.ecospold2.Exchange e2Exchange) {
		e2Exchange.setName(exchange.getFlow().getName());
		e2Exchange.setId(new UUID(exchange.getId(), 0L).toString());
		e2Exchange.setAmount(exchange.getAmountValue());
		e2Exchange.setUnitId(exchange.getUnit().getRefId());
		e2Exchange.setUnitName(exchange.getUnit().getName());
		e2Exchange.setMathematicalRelation(exchange.getAmountFormula());
		e2Exchange.setCasNumber(exchange.getFlow().getCasNumber());
		e2Exchange.setUncertainty(UncertaintyConverter.fromOpenLCA(exchange
				.getUncertainty()));
	}

	private void mapParameters(Process process, DataSet dataSet) {
		List<Parameter> parameters = new ArrayList<>();
		parameters.addAll(process.getParameters());
		ParameterDao dao = new ParameterDao(database);
		parameters.addAll(dao.getGlobalParameters());
		for (Parameter param : parameters) {
			org.openlca.ecospold2.Parameter e2Param = new org.openlca.ecospold2.Parameter();
			e2Param.setName(param.getName());
			e2Param.setId(new UUID(param.getId(), 0L).toString());
			e2Param.setAmount(param.getValue());
			e2Param.setVariableName(param.getName());
			e2Param.setMathematicalRelation(param.getFormula());
			e2Param.setIsCalculatedAmount(!param.isInputParameter());
			// removed because this field does not exist in the schema
			// documentation
			if (param.getScope() != null)
				e2Param.setScope(param.getScope().name());
			e2Param.setUncertainty(UncertaintyConverter.fromOpenLCA(param
					.getUncertainty()));
			dataSet.getParameters().add(e2Param);
		}
	}

	private void mapRepresentativeness(ProcessDocumentation doc, DataSet dataSet) {
		Representativeness repri = new Representativeness();
		repri.setSystemModelId("06590a66-662a-4885-8494-ad0cf410f956");
		repri.setSystemModelName("Allocation, ecoinvent default");
		repri.setSamplingProcedure(doc.getSampling());
		repri.setExtrapolations(doc.getDataTreatment());
		dataSet.setRepresentativeness(repri);
	}

	private void mapAdminInfo(ProcessDocumentation doc, DataSet dataSet) {
		AdministrativeInformation adminInfo = new AdministrativeInformation();
		dataSet.setAdministrativeInformation(adminInfo);
		mapDataEntry(doc.getDataDocumentor(), adminInfo);
		mapDataGenerator(doc, adminInfo);
		mapFileAttributes(doc, adminInfo);
	}

	private void mapDataEntry(Actor dataDocumentor,
			AdministrativeInformation adminInfo) {
		DataEntryBy dataEntryBy = new DataEntryBy();
		adminInfo.setDataEntryBy(dataEntryBy);
		if (dataDocumentor == null) {
			dataEntryBy.setIsActiveAuthor(false);
			dataEntryBy.setPersonEmail("no@email.com");
			dataEntryBy.setPersonId("788d0176-a69c-4de0-a5d3-259866b6b100");
			dataEntryBy.setPersonName("[Current User]");
		} else {
			dataEntryBy.setPersonEmail(dataDocumentor.getEmail());
			dataEntryBy.setPersonId(dataDocumentor.getRefId());
			dataEntryBy.setPersonName(dataDocumentor.getName());
		}
	}

	private void mapDataGenerator(ProcessDocumentation doc,
			AdministrativeInformation adminInfo) {
		DataGenerator dataGenerator = new DataGenerator();
		adminInfo.setDataGenerator(dataGenerator);
		Actor actor = doc.getDataGenerator();
		if (actor == null) {
			dataGenerator.setPersonEmail("no@email.com");
			dataGenerator.setPersonId("788d0176-a69c-4de0-a5d3-259866b6b100");
			dataGenerator.setPersonName("[Current User]");
		} else {
			dataGenerator.setPersonEmail(actor.getEmail());
			dataGenerator.setPersonId(actor.getRefId());
			dataGenerator.setPersonName(actor.getName());
		}
		Source source = doc.getPublication();
		if (source != null) {
			dataGenerator.setPublishedSourceId(source.getRefId());
			dataGenerator.setPublishedSourceFirstAuthor(source.getName());
			if (source.getYear() != null)
				dataGenerator.setPublishedSourceYear(source.getYear()
						.intValue());
		}
		dataGenerator.setCopyrightProtected(doc.isCopyright());
	}

	private void mapFileAttributes(ProcessDocumentation doc,
			AdministrativeInformation adminInfo) {
		FileAttributes atts = new FileAttributes();
		adminInfo.setFileAttributes(atts);
		atts.setMajorRelease(1);
		atts.setMajorRevision(0);
		atts.setMinorRelease(1);
		atts.setMinorRevision(0);
		atts.setDefaultLanguage("en");
		if (doc.getCreationDate() != null)
			atts.setCreationTimestamp(doc.getCreationDate());
		else
			atts.setCreationTimestamp(new Date());
		if (doc.getLastChange() != null)
			atts.setLastEditTimestamp(doc.getLastChange());
		else
			atts.setLastEditTimestamp(new Date());
		atts.setInternalSchemaVersion("1.0");
		atts.setFileGenerator("openLCA");
		atts.setFileTimestamp(new Date());
	}

}
