package org.openlca.io.simapro.csv.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.io.Categories;
import org.openlca.io.UnitMappingEntry;
import org.openlca.simapro.csv.model.annotations.BlockHandler;
import org.openlca.simapro.csv.model.process.ProcessBlock;
import org.openlca.simapro.csv.model.process.ProductOutputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessHandler {

	private Logger log = LoggerFactory.getLogger(getClass());

	private IDatabase database;
	private RefData refData;
	private ProcessDao dao;

	// currently mapped process and process block
	private Process process;
	private ProcessBlock block;
	private ProcessParameterMapper parameterMapper;

	public ProcessHandler(IDatabase database, RefData refData) {
		this.database = database;
		this.refData = refData;
		this.dao = new ProcessDao(database);
	}

	@BlockHandler
	public void handleProcess(ProcessBlock block) {
		String refId = block.getIdentifier();
		Process process = dao.getForRefId(refId);
		if (process != null) {
			log.warn("a process with the identifier {} is already in the "
					+ "database and was not imported", refId);
		}
		log.trace("import process {}", refId);
		process = new Process();
		process.setRefId(refId);
		process.setDefaultAllocationMethod(AllocationMethod.PHYSICAL);
		process.setDocumentation(new ProcessDocumentation());
		this.process = process;
		this.block = block;
		mapData();
		try {
			dao.insert(process);
		} catch (Exception e) {
			log.error("failed to insert process " + refId, e);
		}
		this.process = null;
	}

	private void mapData() {
		mapName();
		mapLocation();
		mapCategory();
		mapType();
		new ProcessDocMapper(database, refData).map(block, process);
		parameterMapper = new ProcessParameterMapper(database);
		long scope = parameterMapper.map(block, process);
		mapProductOutputs(scope);
	}

	private void mapName() {
		if (block.getName() != null) {
			process.setName(block.getName());
			return;
		}
		Flow refFlow = getRefFlow();
		if (refFlow != null) {
			process.setName(refFlow.getName());
			return;
		}
		process.setName(block.getIdentifier());
	}

	private void mapLocation() {
		Flow refFlow = getRefFlow();
		if (refFlow == null)
			return;
		process.setLocation(refFlow.getLocation());
	}

	private Flow getRefFlow() {
		if (!block.getProducts().isEmpty()) {
			Flow flow = refData.getFlow(block.getProducts().get(0));
			if (flow != null)
				return flow;
		}
		// TODO: waste flows
		return null;
	}

	private void mapProductOutputs(long scopeId) {
		int i = 0;
		for (ProductOutputRow row : block.getProducts()) {
			Flow flow = refData.getFlow(row);
			if (flow == null)
				continue;
			Exchange exchange = new Exchange();
			exchange.setFlow(flow);
			setExchangeUnit(exchange, flow, row.getUnit());
			setAmount(exchange, row.getAmount(), scopeId);
			process.getExchanges().add(exchange);
			if (i == 0)
				process.setQuantitativeReference(exchange);
			i++;
			// TODO: map uncertainty
		}

	}

	private void setExchangeUnit(Exchange exchange, Flow flow, String unit) {
		UnitMappingEntry entry = refData.getUnitMapping().getEntry(unit);
		if (entry == null) {
			log.error("unknown unit {}; could not set exchange unit", unit);
			return;
		}
		exchange.setUnit(entry.getUnit());
		FlowPropertyFactor factor = flow.getFactor(entry.getFlowProperty());
		exchange.setFlowPropertyFactor(factor);
	}

	private void setAmount(Exchange exchange, String amountText, long scope) {
		try {
			double val = Double.parseDouble(amountText);
			exchange.setAmountValue(val);
		} catch (Exception e) {
			double val = parameterMapper.eval(amountText, scope);
			exchange.setAmountValue(val);
			exchange.setAmountFormula(amountText);
		}
	}

	private void mapCategory() {
		String categoryPath = null;
		if (!block.getProducts().isEmpty()) {
			ProductOutputRow row = block.getProducts().get(0);
			categoryPath = row.getCategory();
		}
		// TODO: waste treatments
		if (categoryPath == null)
			return;
		String[] path = categoryPath.split("\\\\");
		Category category = Categories.findOrAdd(database, ModelType.PROCESS,
				path);
		process.setCategory(category);
	}

	private void mapType() {
		org.openlca.simapro.csv.model.enums.ProcessType type = block
				.getProcessType();
		if (type == org.openlca.simapro.csv.model.enums.ProcessType.SYSTEM)
			process.setProcessType(ProcessType.LCI_RESULT);
		else
			process.setProcessType(ProcessType.UNIT_PROCESS);
	}

}
