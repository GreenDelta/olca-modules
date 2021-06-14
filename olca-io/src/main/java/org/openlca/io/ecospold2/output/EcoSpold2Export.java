package org.openlca.io.ecospold2.output;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.io.ecospold2.UncertaintyConverter;
import org.openlca.io.maps.FlowMap;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spold2.Activity;
import spold2.ActivityDescription;
import spold2.ActivityName;
import spold2.Compartment;
import spold2.DataSet;
import spold2.EcoSpold2;
import spold2.ElementaryExchange;
import spold2.FlowData;
import spold2.Geography;
import spold2.IntermediateExchange;
import spold2.RichText;
import spold2.UserMasterData;

/**
 * Exports a set of processes to the EcoSpold 2 data format to a directory. The
 * process data sets are converted to EcoSpold 2 activity data sets and written
 * to the sub-folder 'Activities' in a given export directory.
 */
public class EcoSpold2Export implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final File activityDir;
	private final IDatabase db;
	private final List<ProcessDescriptor> descriptors;
	private ElemFlowMap elemFlowMap;

	public EcoSpold2Export(File dir, IDatabase db) {
		this(dir, db, Collections.emptyList());
	}

	public EcoSpold2Export(File dir, IDatabase db, List<ProcessDescriptor> descriptors) {
		this.activityDir = new File(dir, "Activities");
		this.db = db;
		this.descriptors = descriptors;
		this.elemFlowMap = new ElemFlowMap(FlowMap.empty());
	}

	public void setFlowMap(FlowMap map) {
		if (map != null) {
			elemFlowMap = new ElemFlowMap(map);
		}
	}

	@Override
	public void run() {
		try {
			if (!activityDir.exists()) {
				Files.createDirectories(activityDir.toPath());
			}
			exportProcesses();
		} catch (Exception e) {
			log.error("EcoSpold 2 export failed", e);
		}
	}

	private void exportProcesses() {
		for (ProcessDescriptor descriptor : descriptors) {
			ProcessDao dao = new ProcessDao(db);
			Process process = dao.getForId(descriptor.id);
			ProcessDocumentation doc = process.documentation;
			if (doc == null) {
				log.warn("no process entity or documentation for {} found",
						descriptor);
				continue;
			}
			exportProcess(process);
		}
	}

	public void exportProcess(Process process) {
		var ds = new DataSet();
		ds.description = new ActivityDescription();
		ds.masterData = new UserMasterData();
		mapActivity(process, ds);
		mapLocation(process, ds);
		ProcessDoc.map(process, ds);
		mapExchanges(process, ds);
		mapParameters(process, ds);
		MasterData.writeIndexEntry(ds);
		var fileName = process.refId == null
				? UUID.randomUUID().toString()
				: process.refId;
		var file = new File(activityDir, fileName + ".spold");
		EcoSpold2.write(ds, file);
	}

	private void mapActivity(Process process, DataSet ds) {
		var activity = new Activity();
		ds.description.activity = activity;
		var activityName = new ActivityName();
		ds.masterData.activityNames.add(activityName);
		String nameId = UUID.randomUUID().toString();
		activity.activityNameId = nameId;
		activityName.id = nameId;
		String name = Strings.cut(process.name, 120);
		activity.name = name;
		activityName.name = name;
		activity.id = process.refId;
		activity.type = process.processType == ProcessType.LCI_RESULT ? 2 : 1;
		activity.specialActivityType = 0; // default
		activity.generalComment = RichText.of(process.description);
		if (!Strings.nullOrEmpty(process.tags)) {
			Arrays.stream(process.tags.split(","))
					.filter(tag -> !tag.isBlank())
					.forEach(activity.tags::add);
		}
	}

	private void mapLocation(Process process, DataSet ds) {
		if (ds.description == null) {
			ds.description = new ActivityDescription();
		}
		var geo = new Geography();
		ds.description.geography = geo;
		if (process.documentation != null) {
			geo.comment = RichText.of(process.documentation.geography);
		}
		if (process.location == null) {
			// set defaults
			geo.id = "34dbbff8-88ce-11de-ad60-0019e336be3a";
			geo.shortName = "GLO";
		} else {
			geo.id = process.location.refId;
			geo.shortName = process.location.code;
		}
	}

	private void mapExchanges(Process process, DataSet ds) {
		if (ds.flowData == null)
			ds.flowData = new FlowData();
		for (Exchange exchange : process.exchanges) {
			if (!isValid(exchange))
				continue;
			Flow flow = exchange.flow;
			UserMasterData masterData = ds.masterData;
			if (flow.flowType == FlowType.ELEMENTARY_FLOW) {
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
		var e2Ex = elemFlowMap.apply(exchange);
		if (e2Ex != null)
			return e2Ex;
		e2Ex = new ElementaryExchange();
		if (exchange.isInput) {
			e2Ex.inputGroup = 4;
		} else {
			e2Ex.outputGroup = 4;
		}

		var flow = exchange.flow;
		e2Ex.flowId = flow.refId;
		e2Ex.formula = flow.formula;
		mapExchangeData(exchange, e2Ex);

		// compartment
		if (flow.category != null) {
			var comp = new Compartment();
			comp.id = flow.category.refId;
			comp.subCompartment = flow.category.name;
			if (flow.category.category != null) {
				comp.compartment = flow.category.category.name;
			}
		}

		Units.map(exchange.unit, e2Ex, masterData);
		MasterData.writeElemFlow(e2Ex, masterData);
		return e2Ex;
	}

	private IntermediateExchange createIntermediateExchange(Exchange exchange,
			Process process, UserMasterData masterData) {
		IntermediateExchange e2Ex = new IntermediateExchange();
		if (exchange.isInput)
			e2Ex.inputGroup = 5;
		else {
			if (Objects.equals(exchange, process.quantitativeReference))
				e2Ex.outputGroup = 0;
			else if (exchange.flow.flowType == FlowType.WASTE_FLOW)
				e2Ex.outputGroup = 3;
			else
				e2Ex.outputGroup = 2;
		}
		e2Ex.flowId = exchange.flow.refId;
		ProcessDescriptor provider = getDefaultProvider(exchange);
		if (provider != null)
			e2Ex.activityLinkId = provider.refId;
		mapExchangeData(exchange, e2Ex);
		Units.map(exchange.unit, e2Ex, masterData);
		MasterData.writeTechFlow(e2Ex, masterData);
		return e2Ex;
	}

	private ProcessDescriptor getDefaultProvider(Exchange exchange) {
		if (exchange.defaultProviderId == 0)
			return null;
		if (exchange.flow.flowType == FlowType.PRODUCT_FLOW)
			if (!exchange.isInput && !exchange.isAvoided)
				return null;
		if (exchange.flow.flowType == FlowType.WASTE_FLOW)
			if (exchange.isInput && !exchange.isAvoided)
				return null;		
		ProcessDao dao = new ProcessDao(db);
		return dao.getDescriptor(exchange.defaultProviderId);
	}

	private void mapExchangeData(Exchange exchange,
			spold2.Exchange e2Exchange) {
		e2Exchange.name = Strings.cut(exchange.flow.name, 120);
		e2Exchange.id = new UUID(exchange.id, 0L).toString();
		e2Exchange.amount = exchange.amount;
		e2Exchange.mathematicalRelation = exchange.formula;
		e2Exchange.comment = exchange.description;
		e2Exchange.casNumber = exchange.flow.casNumber;
		e2Exchange.uncertainty = UncertaintyConverter.fromOpenLCA(exchange.uncertainty);
	}

	private void mapParameters(Process process, DataSet ds) {
		if (ds.flowData == null)
			ds.flowData = new FlowData();
		var parameters = new ArrayList<>(process.parameters);
		var dao = new ParameterDao(db);
		parameters.addAll(dao.getGlobalParameters());
		for (var param : parameters) {
			var e2Param = new spold2.Parameter();
			e2Param.name = param.name;
			e2Param.id = new UUID(param.id, 0L).toString();
			e2Param.amount = param.value;
			e2Param.variableName = param.name;
			e2Param.mathematicalRelation = param.formula;
			e2Param.isCalculatedAmount = !param.isInputParameter;
			if (param.scope != null)
				e2Param.scope = param.scope.name();
			e2Param.uncertainty = UncertaintyConverter.fromOpenLCA(param.uncertainty);
			ds.flowData.parameters.add(e2Param);
		}
	}

}
