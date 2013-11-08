package org.openlca.io.ecospold2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openlca.core.database.BaseDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.ecospold2.Activity;
import org.openlca.ecospold2.Classification;
import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.ElementaryExchange;
import org.openlca.ecospold2.IntermediateExchange;
import org.openlca.io.KeyGen;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessImport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;
	private RefDataIndex index;
	private ProcessDao dao;
	private final ImportConfig config;

	/** Exchanges that wait for a default provider: provider-id -> exchanges. */
	private HashMap<String, List<Exchange>> linkQueue = new HashMap<>();

	public ProcessImport(IDatabase database, RefDataIndex index,
			ImportConfig config) {
		this.database = database;
		this.index = index;
		this.config = config;
		dao = new ProcessDao(database);
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
		Activity activity = dataSet.getActivity();
		try {
			String refId = RefId.forProcess(dataSet);
			boolean contains = dao.contains(refId);
			if (contains) {
				log.trace("process {} is already in the database",
						activity.getId());
				return;
			}
			log.trace("import process {}", activity.getName());
			runImport(dataSet, refId);
		} catch (Exception e) {
			log.error("Failed to import process", e);
		}
	}

	private boolean valid(DataSet dataSet) {
		Activity activity = dataSet.getActivity();
		if (activity.getId() == null || activity.getName() == null)
			return false;
		IntermediateExchange refFlow = null;
		for (IntermediateExchange techFlow : dataSet.getIntermediateExchanges()) {
			if (techFlow.getOutputGroup() == null)
				continue;
			if (techFlow.getOutputGroup() != 0)
				continue;
			if (techFlow.getAmount() == 0)
				continue;
			refFlow = techFlow;
			break;
		}
		return refFlow != null;
	}

	private void runImport(DataSet dataSet, String refId) {
		Activity activity = dataSet.getActivity();
		Process process = new Process();
		process.setRefId(refId);
		process.setName(activity.getName());
		setCategory(dataSet, process);
		if (config.withParameters)
			process.getParameters().addAll(Parameters.fetch(dataSet, config));
		createProductExchanges(dataSet, process);
		if (process.getQuantitativeReference() == null)
			log.warn("could not set a quantitative reference for process {}",
					refId);
		createElementaryExchanges(dataSet, process);
		new DocImportMapper(database).map(dataSet, process);
		database.createDao(Process.class).insert(process);
		index.putProcessId(refId, process.getId());
		flushLinkQueue(process);
	}

	private void flushLinkQueue(Process process) {
		List<Exchange> exchanges = linkQueue.remove(process.getRefId());
		if (exchanges == null || process.getId() == 0)
			return;
		try {
			BaseDao<Exchange> dao = database.createDao(Exchange.class);
			for (Exchange exchange : exchanges) {
				exchange.setDefaultProviderId(process.getId());
				dao.update(exchange);
			}
		} catch (Exception e) {
			log.error("failed to update default provider", e);
		}
	}

	private void createElementaryExchanges(DataSet dataSet, Process process) {
		for (ElementaryExchange e : dataSet.getElementaryExchanges()) {
			if (e.getAmount() == 0 && config.skipNullExchanges)
				continue;
			String refId = e.getElementaryExchangeId();
			Flow flow = index.getFlow(refId);
			if (flow == null) {
				log.warn("could not create flow for {}",
						e.getElementaryExchangeId());
			}
			createExchange(e, refId, flow, process);
		}
	}

	private void createProductExchanges(DataSet dataSet, Process process) {
		for (IntermediateExchange e : dataSet.getIntermediateExchanges()) {
			boolean isRefFlow = e.getOutputGroup() != null
					&& e.getOutputGroup() == 0;
			if (e.getAmount() == 0 && config.skipNullExchanges)
				continue;
			String refId = RefId.forProductFlow(dataSet, e);
			Flow flow = index.getFlow(refId);
			if (flow == null) {
				log.warn("could not get flow for {}", refId);
				continue;
			}
			Exchange exchange = createExchange(e, refId, flow, process);
			if (exchange == null)
				continue;
			if (isAvoidedProduct(refId, exchange))
				exchange.setAvoidedProduct(true);
			if (e.getActivityLinkId() != null)
				addActivityLink(e, exchange);
			if (isRefFlow)
				process.setQuantitativeReference(exchange);
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

	private Exchange createExchange(org.openlca.ecospold2.Exchange original,
			String flowRefId, Flow flow, Process process) {
		if (flow == null || flow.getReferenceFlowProperty() == null)
			return null;
		Exchange exchange = new Exchange();
		exchange.setFlow(flow);
		exchange.setFlowPropertyFactor(flow.getReferenceFactor());
		Unit unit = getFlowUnit(original, flowRefId, flow);
		if (unit == null)
			return null;
		exchange.setUnit(unit);
		exchange.setInput(original.getInputGroup() != null);
		double amount = original.getAmount();
		if (index.isMappedFlow(flowRefId))
			amount = amount * index.getMappedFlowFactor(flowRefId);
		exchange.setAmountValue(amount);
		// we could switch the input/output side for negative inputs
		// but this could be a problem with waste treatment processes
		// if (amount > 0)
		// exchange.setAmountValue(amount);
		// else {
		// // switch input / output side for negative values
		// exchange.setInput(!exchange.isInput());
		// exchange.setAmountValue(Math.abs(amount));
		// }
		if (config.withParameters && config.withParameterFormulas)
			mapFormula(original, process, exchange);
		exchange.setUncertainty(UncertaintyConverter.toOpenLCA(original
				.getUncertainty()));
		process.getExchanges().add(exchange);
		return exchange;
	}

	private Unit getFlowUnit(org.openlca.ecospold2.Exchange original,
			String flowRefId, Flow flow) {
		if (!index.isMappedFlow(flowRefId))
			return index.getUnit(original.getUnitId());
		FlowProperty refProp = flow.getReferenceFlowProperty();
		if (refProp == null)
			return null;
		UnitGroup ug = refProp.getUnitGroup();
		if (ug == null)
			return null;
		return ug.getReferenceUnit();
	}

	private void mapFormula(org.openlca.ecospold2.Exchange original,
			Process process, Exchange exchange) {
		String var = original.getVariableName();
		if (Strings.notEmpty(var)) {
			if (Parameters.contains(var, process.getParameters()))
				exchange.setAmountFormula(var);
		} else if (Parameters.isValid(original.getMathematicalRelation(),
				config)) {
			exchange.setAmountFormula(original.getMathematicalRelation().trim());
		}
	}

	private void addActivityLink(IntermediateExchange e, Exchange exchange) {
		String providerId = e.getActivityLinkId();
		String flowId = e.getIntermediateExchangeId();
		String refId = KeyGen.get(providerId, flowId);
		Long processId = index.getProcessId(refId);
		if (processId != null) {
			exchange.setDefaultProviderId(processId);
			return;
		}
		List<Exchange> exchanges = linkQueue.get(refId);
		if (exchanges == null) {
			exchanges = new ArrayList<>();
			linkQueue.put(refId, exchanges);
		}
		exchanges.add(exchange);
	}

	private void setCategory(DataSet dataSet, Process process) {
		Category category = null;
		for (Classification clazz : dataSet.getClassifications()) {
			category = index.getProcessCategory(clazz.getClassificationId());
			if (category != null)
				break;
		}
		process.setCategory(category);
	}

}