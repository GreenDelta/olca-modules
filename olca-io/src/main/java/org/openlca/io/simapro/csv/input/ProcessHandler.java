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
import org.openlca.simapro.csv.model.AbstractExchangeRow;
import org.openlca.simapro.csv.model.annotations.BlockHandler;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ProductType;
import org.openlca.simapro.csv.model.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.model.process.ProcessBlock;
import org.openlca.simapro.csv.model.process.ProductExchangeRow;
import org.openlca.simapro.csv.model.process.ProductOutputRow;
import org.openlca.simapro.csv.model.process.RefProductRow;
import org.openlca.simapro.csv.model.refdata.ElementaryFlowRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessHandler {

	private Logger log = LoggerFactory.getLogger(getClass());

	private IDatabase database;
	private RefData refData;
	private ProcessDao dao;
	private FlowHandler flowHandler;

	// currently mapped process and process block
	private Process process;
	private ProcessBlock block;
	private ProcessParameterMapper parameterMapper;

	public ProcessHandler(IDatabase database, RefData refData) {
		this.database = database;
		this.refData = refData;
		this.dao = new ProcessDao(database);
		this.flowHandler = new FlowHandler(database);
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
		mapProductInputs(scope);
		mapElementaryFlows(scope);
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
		if (block.getWasteTreatment() != null)
			return refData.getFlow(block.getWasteTreatment());
		return null;
	}

	private void mapProductOutputs(long scopeId) {
		boolean first = true;
		for (ProductOutputRow row : block.getProducts()) {
			Exchange exchange = createProductOutput(row, scopeId);
			if (first && exchange != null) {
				process.setQuantitativeReference(exchange);
				first = false;
			}
		}
		if (block.getWasteTreatment() != null) {
			Exchange exchange = createProductOutput(block.getWasteTreatment(),
					scopeId);
			process.setQuantitativeReference(exchange);
		}
	}

	private void mapProductInputs(long scope) {
		for (ProductType type : ProductType.values()) {
			for (ProductExchangeRow row : block.getProductExchanges(type)) {
				Flow flow = refData.getFlow(row);
				if (flow == null) {
					flow = flowHandler.getProductFlow(row, type);
					refData.put(row, flow);
				}
				Exchange exchange = createExchange(row, scope, flow);
				if (exchange != null) {
					exchange.setInput(true);
					exchange.setAvoidedProduct(type == ProductType.AVOIDED_PRODUCTS);
				}
			}
		}
	}

	private void mapElementaryFlows(long scope) {
		for(ElementaryFlowType type : ElementaryFlowType.values()) {
			boolean isInputType = type == ElementaryFlowType.RESOURCES;
			for(ElementaryExchangeRow row : block.getElementaryExchangeRows(type)) {
				Flow flow = refData.getFlow(row, type);
				if(flow == null) {
					ElementaryFlowRow flowInfo = refData.getFlowInfo(row, type);
					flow = flowHandler.getElementaryFlow(row, type, flowInfo);
					refData.put(row, type, flow);
				}
				Exchange exchange = createExchange(row, scope, flow);
				if(exchange != null)
					exchange.setInput(isInputType);
			}
		}
	}

	private Exchange createProductOutput(RefProductRow row, long scopeId) {
		Flow flow = refData.getFlow(row);
		Exchange exchange = createExchange(row, scopeId, flow);
		if (exchange == null)
			return null;
		exchange.setInput(false);
		return exchange;
	}

	private Exchange createExchange(AbstractExchangeRow row, long scopeId,
			Flow flow) {
		if (flow == null) {
			log.warn("could not create exchange as there was now flow found " +
					"for {}", row);
			return null;
		}
		Exchange exchange = new Exchange();
		exchange.setFlow(flow);
		setExchangeUnit(exchange, flow, row.getUnit());
		setAmount(exchange, row.getAmount(), scopeId);
		process.getExchanges().add(exchange);
		// TODO: map uncertainty
		return exchange;
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
		} else if (block.getWasteTreatment() != null)
			categoryPath = block.getWasteTreatment().getCategory();
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
