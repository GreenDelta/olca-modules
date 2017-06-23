package org.openlca.io.ecospold2.output;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.io.ecospold2.UncertaintyConverter;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spold2.Activity;
import spold2.ActivityDescription;
import spold2.ActivityName;
import spold2.DataSet;
import spold2.EcoSpold2;
import spold2.ElementaryExchange;
import spold2.FlowData;
import spold2.IntermediateExchange;
import spold2.RichText;
import spold2.UserMasterData;

/**
 * Exports a set of processes to the EcoSpold 2 data format to a directory. The
 * process data sets are converted to EcoSpold 2 activity data sets and written
 * to the sub-folder 'Activities' in a given export directory.
 */
public class EcoSpold2Export implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final File dir;
	private final IDatabase database;
	private final List<ProcessDescriptor> descriptors;

	private final LocationMap locationMap;
	private final UnitMap unitMap;
	private final CompartmentMap compartmentMap;
	private final ElemFlowMap elemFlowMap;

	public EcoSpold2Export(File dir, IDatabase database,
			List<ProcessDescriptor> descriptors) {
		this.dir = dir;
		this.database = database;
		this.descriptors = descriptors;
		this.locationMap = new LocationMap(database);
		this.unitMap = new UnitMap(database);
		this.compartmentMap = new CompartmentMap(database);
		this.elemFlowMap = new ElemFlowMap(database);
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
			exportProcess(activityDir, process);
		}
	}

	private void exportProcess(File activityDir, Process process)
			throws Exception {
		DataSet dataSet = new DataSet();
		dataSet.description = new ActivityDescription();
		UserMasterData masterData = new UserMasterData();
		dataSet.masterData = masterData;
		mapActivity(process, dataSet);
		locationMap.apply(process, dataSet);
		ProcessDoc.map(process, dataSet);
		mapExchanges(process, dataSet);
		mapParameters(process, dataSet);
		MasterData.writeIndexEntry(dataSet);
		String fileName = process.getRefId() == null ? UUID.randomUUID()
				.toString() : process.getRefId();
		File file = new File(activityDir, fileName + ".spold");
		EcoSpold2.write(dataSet, file);
	}

	private void mapActivity(Process process, DataSet dataSet) {
		Activity activity = new Activity();
		dataSet.description.activity = activity;
		ActivityName activityName = new ActivityName();
		dataSet.masterData.activityNames.add(activityName);
		String nameId = UUID.randomUUID().toString();
		activity.activityNameId = nameId;
		activityName.id = nameId;
		String name = Strings.cut(process.getName(), 120);
		activity.name = name;
		activityName.name = name;
		activity.id = process.getRefId();
		int type = process.getProcessType() == ProcessType.LCI_RESULT ? 2 : 1;
		activity.type = type;
		activity.specialActivityType = 0; // default
		activity.generalComment = RichText.of(process.getDescription());
	}

	private void mapExchanges(Process process, DataSet ds) {
		if (ds.flowData == null)
			ds.flowData = new FlowData();
		for (Exchange exchange : process.getExchanges()) {
			if (!isValid(exchange))
				continue;
			Flow flow = exchange.flow;
			UserMasterData masterData = ds.masterData;
			if (flow.getFlowType() == FlowType.ELEMENTARY_FLOW) {
				ElementaryExchange e = createElemExchange(exchange, masterData);
				ds.flowData.elementaryExchanges.add(e);
			} else {
				IntermediateExchange e = createIntermediateExchange(exchange,
						process, masterData);
				ds.flowData.intermediateExchanges.add(e);
			}
		}
	}

	private boolean isValid(Exchange exchange) {
		return exchange.flow != null
				&& exchange.flowPropertyFactor != null
				&& exchange.unit != null;
	}

	private ElementaryExchange createElemExchange(Exchange exchange,
			UserMasterData masterData) {
		ElementaryExchange e2Ex = elemFlowMap.apply(exchange);
		if (e2Ex != null)
			return e2Ex;
		e2Ex = new ElementaryExchange();
		if (exchange.isInput)
			e2Ex.inputGroup = 4;
		else
			e2Ex.outputGroup = 4;
		Flow flow = exchange.flow;
		e2Ex.flowId = flow.getRefId();
		e2Ex.formula = flow.getFormula();
		mapExchangeData(exchange, e2Ex);
		compartmentMap.apply(flow.getCategory(), e2Ex);
		unitMap.apply(exchange.unit, e2Ex, masterData);
		MasterData.writeElemFlow(e2Ex, masterData);
		return e2Ex;
	}

	private IntermediateExchange createIntermediateExchange(Exchange exchange,
			Process process, UserMasterData masterData) {
		IntermediateExchange e2Ex = new IntermediateExchange();
		if (exchange.isInput)
			e2Ex.inputGroup = 5;
		else {
			if (Objects.equals(exchange, process.getQuantitativeReference()))
				e2Ex.outputGroup = 0;
			else if (exchange.flow.getFlowType() == FlowType.WASTE_FLOW)
				e2Ex.outputGroup = 3;
			else
				e2Ex.outputGroup = 2;
		}
		e2Ex.flowId = exchange.flow.getRefId();
		ProcessDescriptor provider = getDefaultProvider(exchange);
		if (provider != null)
			e2Ex.activityLinkId = provider.getRefId();
		mapExchangeData(exchange, e2Ex);
		unitMap.apply(exchange.unit, e2Ex, masterData);
		MasterData.writeTechFlow(e2Ex, masterData);
		return e2Ex;
	}

	private ProcessDescriptor getDefaultProvider(Exchange exchange) {
		if (!exchange.isInput || exchange.defaultProviderId == 0)
			return null;
		ProcessDao dao = new ProcessDao(database);
		return dao.getDescriptor(exchange.defaultProviderId);
	}

	private void mapExchangeData(Exchange exchange,
			spold2.Exchange e2Exchange) {
		e2Exchange.name = Strings.cut(exchange.flow.getName(), 120);
		e2Exchange.id = new UUID(exchange.getId(), 0L).toString();
		e2Exchange.amount = exchange.amount;
		e2Exchange.mathematicalRelation = exchange.amountFormula;
		e2Exchange.comment = exchange.description;
		e2Exchange.casNumber = exchange.flow.getCasNumber();
		e2Exchange.uncertainty = UncertaintyConverter.fromOpenLCA(exchange.uncertainty);
	}

	private void mapParameters(Process process, DataSet ds) {
		if (ds.flowData == null)
			ds.flowData = new FlowData();
		List<Parameter> parameters = new ArrayList<>();
		parameters.addAll(process.getParameters());
		ParameterDao dao = new ParameterDao(database);
		parameters.addAll(dao.getGlobalParameters());
		for (Parameter param : parameters) {
			spold2.Parameter e2Param = new spold2.Parameter();
			e2Param.name = param.getName();
			e2Param.id = new UUID(param.getId(), 0L).toString();
			e2Param.amount = param.getValue();
			e2Param.variableName = param.getName();
			e2Param.mathematicalRelation = param.getFormula();
			e2Param.isCalculatedAmount = !param.isInputParameter();
			if (param.getScope() != null)
				e2Param.scope = param.getScope().name();
			e2Param.uncertainty = UncertaintyConverter.fromOpenLCA(param
					.getUncertainty());
			ds.flowData.parameters.add(e2Param);
		}
	}

}
