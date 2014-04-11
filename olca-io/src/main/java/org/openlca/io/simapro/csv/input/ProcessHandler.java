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
import org.openlca.io.KeyGen;
import org.openlca.io.UnitMappingEntry;
import org.openlca.io.maps.MapFactor;
import org.openlca.simapro.csv.model.AbstractExchangeRow;
import org.openlca.simapro.csv.model.annotations.BlockHandler;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ProductType;
import org.openlca.simapro.csv.model.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.model.process.ProcessBlock;
import org.openlca.simapro.csv.model.process.ProductExchangeRow;
import org.openlca.simapro.csv.model.process.ProductOutputRow;
import org.openlca.simapro.csv.model.process.RefProductRow;
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
		String refId = KeyGen.get(block.getIdentifier());
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
			ProductOutputRow refRow = block.getProducts().get(0);
			Flow flow = refData.getProduct(refRow.getName());
			if (flow != null)
				return flow;
		}
		if (block.getWasteTreatment() != null)
			return refData.getProduct(block.getWasteTreatment().getName());
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
				Flow flow = refData.getProduct(row.getName());
				if (flow == null) {
					log.error("unknown flow {}; could not create exchange", row);
					continue;
				}
				Exchange exchange = addExchange(row, scope, flow);
				if (exchange != null) {
					exchange.setInput(true);
					exchange.setAvoidedProduct(type == ProductType.AVOIDED_PRODUCTS);
				}
			}
		}
	}

	private void mapElementaryFlows(long scope) {
		for (ElementaryFlowType type : ElementaryFlowType.values()) {
			boolean isInputType = type == ElementaryFlowType.RESOURCES;
			for (ElementaryExchangeRow row : block
					.getElementaryExchangeRows(type)) {
				String key = KeyGen.get(row.getName(),
						type.getExchangeHeader(), row.getSubCompartment(),
						row.getUnit());
				MapFactor<Flow> mappedFlow = refData.getMappedFlow(key);
				Exchange exchange;
				if (mappedFlow != null)
					exchange = createMappedExchange(mappedFlow, row, scope);
				else {
					Flow flow = refData.getElemFlow(key);
					exchange = addExchange(row, scope, flow);
				}
				if (exchange != null) {
					exchange.setInput(isInputType);
				}
			}
		}
	}

	private Exchange createMappedExchange(MapFactor<Flow> mappedFlow,
			ElementaryExchangeRow row, long scope) {
		Flow flow = mappedFlow.getEntity();
		Exchange exchange = addExchange(row, scope, flow);
		if (exchange != null) {
			exchange.setAmountValue(mappedFlow.getFactor()
					* exchange.getAmountValue());
			if (exchange.getAmountFormula() != null) {
				String formula = Double.toString(mappedFlow.getFactor())
						+ " * ( " + exchange.getAmountFormula() + " )";
				exchange.setAmountFormula(formula);
			}
		}
		return exchange;
	}

	private Exchange createProductOutput(RefProductRow row, long scopeId) {
		Flow flow = refData.getProduct(row.getName());
		Exchange exchange = addExchange(row, scopeId, flow);
		if (exchange != null)
			exchange.setInput(false);
		return exchange;
	}

	private Exchange addExchange(AbstractExchangeRow row, long scopeId,
			Flow flow) {
		if (flow == null) {
			log.error("could not create exchange as there was now flow found "
					+ "for {}", row);
			return null;
		}
		Exchange exchange = new Exchange();
		exchange.setFlow(flow);
		setExchangeUnit(exchange, flow, row.getUnit());
		setAmount(exchange, row.getAmount(), scopeId);
		process.getExchanges().add(exchange);
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
