package org.openlca.io.ecospold2.output;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.ecospold2.Activity;
import org.openlca.ecospold2.Classification;
import org.openlca.ecospold2.Compartment;
import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.EcoSpold2;
import org.openlca.ecospold2.ElementaryExchange;
import org.openlca.ecospold2.Geography;
import org.openlca.ecospold2.IntermediateExchange;
import org.openlca.ecospold2.MacroEconomicScenario;
import org.openlca.ecospold2.Technology;
import org.openlca.ecospold2.TimePeriod;
import org.openlca.io.ecospold2.UncertaintyConverter;
import org.slf4j.Logger;

import com.google.common.base.Joiner;

/**
 * Exports a set of processes to the EcoSpold 2 data format to a directory. The
 * process data sets are converted to EcoSpold 2 activity data sets and written
 * to the sub-folder 'Activities' in a given export directory. Additionally,
 * EcoSpold 2 master data are created for the exported processes and written to
 * the 'MasterData' sub-folder.
 * 
 */
public class EcoSpold2Export implements Runnable {

	private Logger log;
	private File dir;
	private IDatabase database;
	private List<ProcessDescriptor> descriptors;
	private MasterData masterData;

	public EcoSpold2Export(File dir, IDatabase database,
			List<ProcessDescriptor> descriptors) {
		this.dir = dir;
		this.database = database;
		this.descriptors = descriptors;
	}

	@Override
	public void run() {
		try {
			masterData = new MasterData();
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
			if (process == null)
				continue;
			DataSet dataSet = new DataSet();
			Activity activity = createActivity(process);
			dataSet.setActivity(activity);
			if (process.getCategory() != null)
				dataSet.getClassifications().add(
						convertCategory(process.getCategory()));
			mapGeography(process, dataSet);
			addEconomicScenario(dataSet);
			mapExchanges(process, dataSet);
			if (process.getDocumentation() != null) {
				ProcessDocumentation doc = process.getDocumentation();
				mapTechnology(doc, dataSet);
				mapTime(doc, dataSet);
			}
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
		activity.setActivityNameId(process.getRefId());
		int type = process.getProcessType() == ProcessType.LCI_RESULT ? 2 : 1;
		activity.setType(type);
		activity.setSpecialActivityType(0); // default
		activity.setGeneralComment(process.getDescription());
		return activity;
	}

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
		masterData.classifications.add(category);
		return classification;
	}

	private void mapGeography(Process process, DataSet dataSet) {
		Geography geography = new Geography();
		if (process.getDocumentation() != null)
			geography.setComment(process.getDocumentation().getGeography());
		if (process.getLocation() != null) {
			Location location = process.getLocation();
			masterData.locations.add(location);
			geography.setId(location.getRefId());
			geography.setShortName(location.getCode());
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
		timePeriod.setEndDate(doc.getValidUntil());
		timePeriod.setStartDate(doc.getValidFrom());
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
			FlowDescriptor descriptor = Descriptors.toDescriptor(flow);
			if (flow.getFlowType() == FlowType.ELEMENTARY_FLOW) {
				masterData.elementaryFlows.add(descriptor);
				e2Exchange = createElementaryExchange(exchange);
				dataSet.getElementaryExchanges().add(
						(ElementaryExchange) e2Exchange);
			} else {
				masterData.technosphereFlows.add(descriptor);
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
		masterData.compartments.add(category);
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
		if (exchange.getFlow().getCategory() != null)
			e2Ex.getClassifications().add(
					convertCategory(exchange.getFlow().getCategory()));
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
}
