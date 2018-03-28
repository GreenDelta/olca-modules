package org.openlca.io.ecospold2.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.ExchangeDao;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.ecospold2.UncertaintyConverter;
import org.openlca.util.DQSystems;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import spold2.Activity;
import spold2.Classification;
import spold2.DataSet;
import spold2.ElementaryExchange;
import spold2.IntermediateExchange;
import spold2.PedigreeMatrix;
import spold2.RichText;
import spold2.Spold2;

class ProcessImport {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final RefDataIndex index;
	private final ProcessDao dao;
	private final PriceMapper prices;
	private final ImportConfig config;
	private final DQSystem dqSystem;

	/** Exchanges that wait for a default provider: provider-id -> exchanges. */
	private final HashMap<String, List<Exchange>> linkQueue = new HashMap<>();

	public ProcessImport(RefDataIndex index, ImportConfig config) {
		this.index = index;
		this.config = config;
		dao = new ProcessDao(config.db);
		prices = new PriceMapper(config.db);
		dqSystem = DQSystems.ecoinvent(config.db);
	}

	public void importDataSet(DataSet dataSet) {
		try {
			if (dataSet == null) {
				log.warn("not an EcoSpold data set");
				return;
			}
			checkImport(dataSet);
		} catch (Exception e) {
			log.error("Failed to import EcoSpold 2 process", e);
		}
	}

	private void checkImport(DataSet dataSet) {
		if (!valid(dataSet)) {
			log.warn("invalid data set -> not imported");
			return;
		}
		Activity activity = Spold2.getActivity(dataSet);
		try {
			String refId = RefId.forProcess(dataSet);
			boolean contains = dao.contains(refId);
			if (contains) {
				log.trace("process {} is already in the database",
						activity.id);
				return;
			}
			log.trace("import process {}", activity.name);
			runImport(dataSet, refId);
		} catch (Exception e) {
			log.error("Failed to import process", e);
		}
	}

	private boolean valid(DataSet ds) {
		Activity activity = Spold2.getActivity(ds);
		if (activity.id == null || activity.name == null)
			return false;
		IntermediateExchange refFlow = null;
		for (IntermediateExchange techFlow : Spold2.getProducts(ds)) {
			if (techFlow.outputGroup == null)
				continue;
			if (techFlow.outputGroup != 0)
				continue;
			if (techFlow.amount == 0)
				continue;
			refFlow = techFlow;
			break;
		}
		return refFlow != null;
	}

	private void runImport(DataSet dataSet, String refId) {
		Activity activity = Spold2.getActivity(dataSet);
		Process process = new Process();
		process.setRefId(refId);
		setMetaData(activity, process);
		setCategory(dataSet, process);
		if (config.withParameters)
			handleParameters(dataSet, process);
		createProductExchanges(dataSet, process);
		if (process.getQuantitativeReference() == null)
			log.warn("could not set a quantitative reference for process {}",
					refId);
		createElementaryExchanges(dataSet, process);
		process.exchangeDqSystem = dqSystem;
		new DocImportMapper(config.db).map(dataSet, process);
		new ProcessDao(config.db).insert(process);
		index.putProcessId(refId, process.getId());
		flushLinkQueue(process);
	}

	private void handleParameters(DataSet dataSet, Process process) {
		List<Parameter> list = Parameters.fetch(dataSet, config);
		List<Parameter> newGlobals = new ArrayList<>();
		for (Parameter p : list) {
			if (p.getScope() == ParameterScope.PROCESS)
				process.getParameters().add(p);
			else if (p.getScope() == ParameterScope.GLOBAL)
				newGlobals.add(p);
		}
		ParameterDao dao = new ParameterDao(config.db);
		Map<String, Boolean> map = new HashMap<>();
		for (Parameter p : dao.getGlobalParameters())
			map.put(p.getName(), Boolean.TRUE);
		for (Parameter newGlobal : newGlobals) {
			Boolean exists = map.get(newGlobal.getName());
			if (exists == null) {
				dao.insert(newGlobal);
				map.put(newGlobal.getName(), Boolean.TRUE);
			}
		}
	}

	private void setMetaData(Activity a, Process p) {
		p.setName(a.name);
		ProcessType type = a.type == 2 ? ProcessType.LCI_RESULT
				: ProcessType.UNIT_PROCESS;
		p.setProcessType(type);
		String d = Joiner.on(" ").skipNulls().join(
				RichText.join(a.generalComment),
				a.includedActivitiesStart,
				a.includedActivitiesEnd,
				RichText.join(a.allocationComment));
		p.setDescription(d);
	}

	private void flushLinkQueue(Process process) {
		List<Exchange> exchanges = linkQueue.remove(process.getRefId());
		if (exchanges == null || process.getId() == 0)
			return;
		try {
			ExchangeDao dao = new ExchangeDao(config.db);
			for (Exchange exchange : exchanges) {
				exchange.defaultProviderId = process.getId();
				dao.update(exchange);
			}
		} catch (Exception e) {
			log.error("failed to update default provider", e);
		}
	}

	private void createElementaryExchanges(DataSet ds, Process process) {
		for (ElementaryExchange e : Spold2.getElemFlows(ds)) {
			if (e.amount == 0 && config.skipNullExchanges)
				continue;
			String refId = e.flowId;
			Flow flow = index.getFlow(refId);
			if (flow == null) {
				log.warn("could not create flow for {}",
						e.flowId);
			}
			createExchange(e, refId, flow, process);
		}
	}

	private void createProductExchanges(DataSet ds, Process process) {
		for (IntermediateExchange ie : Spold2.getProducts(ds)) {
			boolean isRefFlow = ie.outputGroup != null
					&& ie.outputGroup == 0;
			if (ie.amount == 0 && config.skipNullExchanges)
				continue;
			String refId = ie.flowId;
			Flow flow = index.getFlow(refId);
			if (flow == null) {
				log.warn("could not get flow for {}", refId);
				continue;
			}
			Exchange e = createExchange(ie, refId, flow, process);
			if (e == null)
				continue;
			if (isAvoidedProduct(refId, e))
				e.isAvoided = true;
			if (ie.activityLinkId != null)
				addActivityLink(ie, e);
			if (isRefFlow)
				process.setQuantitativeReference(e);
			prices.map(ie, e);
		}
	}

	private boolean isAvoidedProduct(String refId, Exchange exchange) {
		return false;
		// If the sign of an product/waste input is different from the sign of
		// the product/waste output of the linked activity it could be an
		// avoided product. Not sure, if this is true for ecoinvent 3
		// boolean isNeg = exchange.getAmountValue() < 0;
		// return isNeg != index.isNegativeFlow(refId) && exchange.isInput();
	}

	private Exchange createExchange(spold2.Exchange es2,
			String flowRefId, Flow flow, Process process) {
		if (flow == null || flow.getReferenceFlowProperty() == null)
			return null;
		Unit unit = getFlowUnit(es2, flowRefId, flow);
		Exchange e = process.exchange(flow, flow.getReferenceFlowProperty(), unit);
		e.description = es2.comment;
		if (unit == null)
			return null;
		final Unit unit1 = unit;
		e.unit = unit1;
		e.isInput = es2.inputGroup != null;
		double amount = es2.amount;
		double f = 1;
		if (index.isMappedFlow(flowRefId)) {
			f = index.getMappedFlowFactor(flowRefId);
		}
		e.amount = amount * f;
		e.uncertainty = UncertaintyConverter.toOpenLCA(es2.uncertainty, f);
		if (config.withParameters && config.withParameterFormulas)
			mapFormula(es2, process, e, f);
		e.dqEntry = getPedigreeMatrix(es2);
		return e;
	}

	private String getPedigreeMatrix(spold2.Exchange es2) {
		if (es2 == null || es2.uncertainty == null)
			return null;
		PedigreeMatrix pm = es2.uncertainty.pedigreeMatrix;
		if (pm == null)
			return null;
		return dqSystem.toString(pm.reliability, pm.completeness, pm.temporalCorrelation,
				pm.geographicalCorrelation, pm.technologyCorrelation);
	}

	private Unit getFlowUnit(spold2.Exchange original,
			String flowRefId, Flow flow) {
		if (!index.isMappedFlow(flowRefId))
			return index.getUnit(original.unitId);
		FlowProperty refProp = flow.getReferenceFlowProperty();
		if (refProp == null)
			return null;
		UnitGroup ug = refProp.getUnitGroup();
		if (ug == null)
			return null;
		return ug.getReferenceUnit();
	}

	private void mapFormula(spold2.Exchange original, Process process,
			Exchange exchange, double factor) {
		String formula = null;
		String var = original.variableName;
		if (Strings.notEmpty(var)
				&& Parameters.contains(var, process.getParameters())) {
			formula = var;
		} else if (Parameters.isValid(original.mathematicalRelation, config)) {
			formula = original.mathematicalRelation;
		}
		if (formula == null)
			return;
		formula = formula.trim();
		if (factor == 1.0)
			exchange.amountFormula = formula;
		else
			exchange.amountFormula = factor + " * (" + formula + ")";
	}

	private void addActivityLink(IntermediateExchange e, Exchange exchange) {
		String providerId = e.activityLinkId;
		String flowId = e.flowId;
		String refId = KeyGen.get(providerId, flowId);
		Long processId = index.getProcessId(refId);
		if (processId != null) {
			exchange.defaultProviderId = processId;
			return;
		}
		List<Exchange> exchanges = linkQueue.get(refId);
		if (exchanges == null) {
			exchanges = new ArrayList<>();
			linkQueue.put(refId, exchanges);
		}
		exchanges.add(exchange);
	}

	private void setCategory(DataSet ds, Process process) {
		Category category = null;
		for (Classification clazz : Spold2.getClassifications(ds)) {
			category = index.getProcessCategory(clazz.id);
			if (category != null)
				break;
		}
		process.setCategory(category);
	}

}