package org.openlca.io.simapro.csv.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.Categories;
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
		mapAllocation();
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

	private void mapAllocation() {
		for (ProductOutputRow output : block.getProducts()) {
			double value = output.getAllocation() / 100d;
			long productId = refData.getProduct(output.getName()).getId();
			addFactor(AllocationMethod.PHYSICAL, productId, value);
			addFactor(AllocationMethod.ECONOMIC, productId, value);
			for (Exchange e : process.getExchanges()) {
				if (!isOutputProduct(e)) {
					addCausalFactor(productId, e, value);
				}
			}
		}
	}

	private boolean isOutputProduct(Exchange e) {
		return e != null && e.flow != null
				&& !e.isInput && !e.isAvoided
				&& e.flow.getFlowType() == FlowType.PRODUCT_FLOW;
	}

	private void addFactor(AllocationMethod method, long productId, double value) {
		AllocationFactor f = new AllocationFactor();
		f.setAllocationType(method);
		f.setValue(value);
		f.setProductId(productId);
		process.getAllocationFactors().add(f);
	}

	private void addCausalFactor(long productId, Exchange e, double value) {
		AllocationFactor f = new AllocationFactor();
		f.setAllocationType(AllocationMethod.CAUSAL);
		f.setValue(value);
		f.setProductId(productId);
		f.setExchange(e);
		process.getAllocationFactors().add(f);
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

	private void mapProductOutputs(long scope) {
		boolean first = true;
		for (ProductOutputRow row : block.getProducts()) {
			Exchange e = createProductOutput(row, scope);
			if (first && e != null) {
				process.setQuantitativeReference(e);
				first = false;
			}
		}
		if (block.getWasteTreatment() != null) {
			Exchange e = createProductOutput(block.getWasteTreatment(),
					scope);
			process.setQuantitativeReference(e);
		}
	}

	private Exchange createProductOutput(RefProductRow row, long scope) {
		Flow flow = refData.getProduct(row.getName());
		Exchange e = initExchange(row, scope, flow);
		if (e == null)
			return null;
		e.isInput = false;
		setUnit(e, row.getUnit());
		process.getExchanges().add(e);
		return e;
	}

	private void mapProductInputs(long scope) {
		for (ProductType type : ProductType.values()) {
			for (ProductExchangeRow row : block.getProductExchanges(type)) {
				Flow flow = refData.getProduct(row.getName());
				Exchange e = initExchange(row, scope, flow);
				if (e == null)
					continue;
				e.isInput = true;
				e.isAvoided = type == ProductType.AVOIDED_PRODUCTS;
				setUnit(e, row.getUnit());
				process.getExchanges().add(e);
			}
		}
	}

	private void mapElementaryFlows(long scope) {
		for (ElementaryFlowType type : ElementaryFlowType.values()) {
			boolean isInput = type == ElementaryFlowType.RESOURCES;
			for (ElementaryExchangeRow row : block
					.getElementaryExchangeRows(type)) {
				String key = KeyGen.get(row.getName(),
						type.getExchangeHeader(), row.getSubCompartment(),
						row.getUnit());
				MapFactor<Flow> factor = refData.getMappedFlow(key);
				Exchange e;
				if (factor != null) {
					e = initMappedExchange(factor, row, scope);
					setRefUnit(e);
				} else {
					Flow flow = refData.getElemFlow(key);
					e = initExchange(row, scope, flow);
					setUnit(e, row.getUnit());
				}
				if (e == null)
					continue;
				e.isInput = isInput;
				process.getExchanges().add(e);
			}
		}
	}

	private Exchange initMappedExchange(MapFactor<Flow> mappedFlow,
			ElementaryExchangeRow row, long scope) {
		Flow flow = mappedFlow.getEntity();
		Exchange e = initExchange(row, scope, flow);
		if (e == null)
			return null;
		double f = mappedFlow.getFactor();
		e.amount = f * e.amount;
		if (e.amountFormula != null) {
			String formula = f + " * ( " + e.amountFormula + " )";
			e.amountFormula = formula;
		}
		if (e.uncertainty != null) {
			e.uncertainty.scale(f);
		}
		return e;
	}

	private Exchange initExchange(AbstractExchangeRow row, long scopeId,
			Flow flow) {
		if (flow == null) {
			log.error("could not create exchange as there was now flow found "
					+ "for {}", row);
			return null;
		}
		Exchange e = new Exchange();
		final Flow flow1 = flow;
		e.flow = flow1;
		e.description = row.getComment();
		setAmount(e, row.getAmount(), scopeId);
		Uncertainty uncertainty = Uncertainties.get(e.amount,
				row.getUncertaintyDistribution());
		e.uncertainty = uncertainty;
		return e;
	}

	/** Sets the exchange unit and flow property for the given SimaPro unit. */
	private void setUnit(Exchange e, String unit) {
		if (e == null || e.flow == null)
			return;
		UnitMappingEntry entry = refData.getUnitMapping().getEntry(unit);
		if (entry == null) {
			log.error("unknown unit {}; could not set exchange unit", unit);
			return;
		}
		Flow flow = e.flow;
		e.unit = entry.unit;
		FlowPropertyFactor factor = flow.getFactor(entry.flowProperty);
		e.flowPropertyFactor = factor;
	}

	/**
	 * Sets the exchange unit and flow property from the flow reference data
	 * (used for mapped reference flows).
	 */
	private void setRefUnit(Exchange e) {
		if (e == null || e.flow == null)
			return;
		Flow f = e.flow;
		FlowPropertyFactor fac = f.getReferenceFactor();
		if (fac == null || fac.getFlowProperty() == null)
			return;
		e.flowPropertyFactor = fac;
		FlowProperty prop = fac.getFlowProperty();
		UnitGroup group = prop.getUnitGroup();
		if (group == null || group.getReferenceUnit() == null)
			return;
		e.unit = group.getReferenceUnit();
	}

	private void setAmount(Exchange e, String amountText, long scope) {
		if (Strings.nullOrEmpty(amountText)) {
			e.amount = (double) 0;
			return;
		}
		try {
			double val = Double.parseDouble(amountText);
			e.amount = val;
		} catch (Exception ex) {
			String formula = amountText.replace(',', '.');
			double val = parameterMapper.eval(formula, scope);
			e.amount = val;
			e.amountFormula = formula;
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
