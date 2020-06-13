package org.openlca.io.simapro.csv.input;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Uncertainty;
import org.openlca.io.UnitMappingEntry;
import org.openlca.io.maps.MapFactor;
import org.openlca.simapro.csv.model.ExchangeRow;
import org.openlca.simapro.csv.model.annotations.BlockHandler;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ProductType;
import org.openlca.simapro.csv.model.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.model.process.ProcessBlock;
import org.openlca.simapro.csv.model.process.ProductExchangeRow;
import org.openlca.simapro.csv.model.process.ProductOutputRow;
import org.openlca.simapro.csv.model.process.RefProductRow;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;
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
		String refId = KeyGen.get(block.identifier);
		Process process = dao.getForRefId(refId);
		if (process != null) {
			log.warn("a process with the identifier {} is already in the "
					+ "database and was not imported", refId);
		}
		log.trace("import process {}", refId);
		process = new Process();
		process.refId = refId;
		process.defaultAllocationMethod = AllocationMethod.PHYSICAL;
		process.documentation = new ProcessDocumentation();
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
		new ProcessDocMapper(refData).map(block, process);
		parameterMapper = new ProcessParameterMapper(database);
		long scope = parameterMapper.map(block, process);
		mapProductOutputs(process, scope);
		mapProductInputs(process, scope);
		mapElementaryFlows(process, scope);
		mapAllocation();
	}

	private void mapName() {
		if (block.name != null) {
			process.name = block.name;
			return;
		}
		Flow refFlow = getRefFlow();
		if (refFlow != null) {
			process.name = refFlow.name;
			return;
		}
		process.name = block.identifier;
	}

	private void mapLocation() {
		Flow refFlow = getRefFlow();
		if (refFlow == null)
			return;
		process.location = refFlow.location;
	}

	private void mapAllocation() {
		for (ProductOutputRow output : block.products) {
			double value = output.allocation / 100d;
			long productId = refData.getProduct(output.name).id;
			addFactor(AllocationMethod.PHYSICAL, productId, value);
			addFactor(AllocationMethod.ECONOMIC, productId, value);
			for (Exchange e : process.exchanges) {
				if (!isOutputProduct(e)) {
					addCausalFactor(productId, e, value);
				}
			}
		}
	}

	private boolean isOutputProduct(Exchange e) {
		return e != null && e.flow != null
				&& !e.isInput && !e.isAvoided
				&& e.flow.flowType == FlowType.PRODUCT_FLOW;
	}

	private void addFactor(AllocationMethod method, long productId,
			double value) {
		AllocationFactor f = new AllocationFactor();
		f.method = method;
		f.value = value;
		f.productId = productId;
		process.allocationFactors.add(f);
	}

	private void addCausalFactor(long productId, Exchange e, double value) {
		AllocationFactor f = new AllocationFactor();
		f.method = AllocationMethod.CAUSAL;
		f.value = value;
		f.productId = productId;
		f.exchange = e;
		process.allocationFactors.add(f);
	}

	private Flow getRefFlow() {
		if (!block.products.isEmpty()) {
			ProductOutputRow refRow = block.products.get(0);
			Flow flow = refData.getProduct(refRow.name);
			if (flow != null)
				return flow;
		}
		if (block.wasteTreatment != null)
			return refData.getProduct(block.wasteTreatment.name);
		return null;
	}

	private void mapProductOutputs(Process process, long scope) {
		boolean first = true;
		for (ProductOutputRow row : block.products) {
			Exchange e = createProductOutput(process, row, scope);
			if (first && e != null) {
				process.quantitativeReference = e;
				first = false;
			}
		}
		if (block.wasteTreatment != null) {
			process.quantitativeReference = createProductOutput(
					process, block.wasteTreatment, scope);
		}
	}

	private Exchange createProductOutput(Process process, RefProductRow row, long scope) {
		Flow flow = refData.getProduct(row.name);
		return initExchange(row, scope, flow, process, false);
	}

	private void mapProductInputs(Process process, long scope) {
		for (ProductType type : ProductType.values()) {
			for (ProductExchangeRow row : block.getProductExchanges(type)) {
				Flow flow = refData.getProduct(row.name);
				Exchange e = initExchange(row, scope, flow, process, false);
				if (e == null)
					continue;
				e.isInput = true;
				e.isAvoided = type == ProductType.AVOIDED_PRODUCTS;
			}
		}
	}

	private void mapElementaryFlows(Process process, long scope) {
		for (ElementaryFlowType type : ElementaryFlowType.values()) {
			boolean isInput = type == ElementaryFlowType.RESOURCES;
			for (ElementaryExchangeRow row : block
					.getElementaryExchangeRows(type)) {
				String key = KeyGen.get(row.name,
						type.getExchangeHeader(), row.subCompartment,
						row.unit);
				MapFactor<Flow> factor = refData.getMappedFlow(key);
				Exchange e;
				if (factor != null) {
					e = initMappedExchange(factor, row, process, scope);
				} else {
					Flow flow = refData.getElemFlow(key);
					e = initExchange(row, scope, flow, process, false);
				}
				if (e == null)
					continue;
				e.isInput = isInput;
			}
		}
	}

	private Exchange initMappedExchange(MapFactor<Flow> mappedFlow,
			ElementaryExchangeRow row, Process process, long scope) {
		Flow flow = mappedFlow.getEntity();
		Exchange e = initExchange(row, scope, flow, process, true);
		if (e == null)
			return null;
		double f = mappedFlow.getFactor();
		e.amount = f * e.amount;
		if (e.formula != null) {
			String formula = f + " * ( " + e.formula + " )";
			e.formula = formula;
		}
		if (e.uncertainty != null) {
			e.uncertainty.scale(f);
		}
		return e;
	}

	private Exchange initExchange(ExchangeRow row, long scopeId,
			Flow flow, Process process, boolean refUnit) {
		if (flow == null) {
			log.error("could not create exchange as there was now flow found " + "for {}", row);
			return null;
		}
		Exchange e = null;
		UnitMappingEntry entry = refData.getUnitMapping().getEntry(row.unit);
		if (refUnit || entry == null) {
			e = process.add(Exchange.of(flow));
			if (!refUnit && entry == null) {
				log.error("unknown unit {}; could not set exchange unit, setting ref unit", row.unit);
			}
		} else {
			e = process.add(Exchange.of(flow, entry.flowProperty, entry.unit));
		}
		e.description = row.comment;
		setAmount(e, row.amount, scopeId);
		Uncertainty uncertainty = Uncertainties.get(e.amount, row.uncertaintyDistribution);
		e.uncertainty = uncertainty;
		return e;
	}

	private void setAmount(Exchange e, String amount, long scope) {
		if (Strings.nullOrEmpty(amount)) {
			e.amount = 0;
			return;
		}
		try {
			double val = Double.parseDouble(amount);
			e.amount = val;
		} catch (Exception ex) {
			double val = parameterMapper.eval(amount, scope);
			e.amount = val;
			e.formula = amount;
		}
	}

	private void mapCategory() {
		String categoryPath = null;
		if (!block.products.isEmpty()) {
			ProductOutputRow row = block.products.get(0);
			categoryPath = row.category;
		} else if (block.wasteTreatment != null)
			categoryPath = block.wasteTreatment.category;
		if (Strings.nullOrEmpty(categoryPath))
			return;
		var path = categoryPath.split("\\\\");
		process.category = new CategoryDao(database)
				.sync(ModelType.PROCESS, path);
	}

	private void mapType() {
		var type = block.processType;
		if (type == null) {
			process.processType = ProcessType.UNIT_PROCESS;
			return;
		}
		switch (type) {
		case SYSTEM:
			process.processType = ProcessType.LCI_RESULT;
			break;
		default:
			process.processType = ProcessType.UNIT_PROCESS;
			break;
		}
	}
}
