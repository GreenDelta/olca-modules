package org.openlca.core.matrix;

import java.util.List;
import java.util.Map;

import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InventoryBuilder {

	private final MatrixCache cache;
	private final ProductIndex productIndex;
	private final AllocationMethod allocationMethod;

	private FlowIndex flowIndex;
	private AllocationIndex allocationTable;
	private ExchangeMatrix technologyMatrix;
	private ExchangeMatrix interventionMatrix;

	InventoryBuilder(MatrixCache matrixCache, ProductIndex productIndex,
			AllocationMethod allocationMethod) {
		this.cache = matrixCache;
		this.productIndex = productIndex;
		this.allocationMethod = allocationMethod;
	}

	Inventory build() {
		if (allocationMethod != null
				&& allocationMethod != AllocationMethod.NONE)
			allocationTable = AllocationIndex.create(productIndex,
					allocationMethod, cache);
		flowIndex = FlowIndex.build(cache, productIndex, allocationMethod);
		technologyMatrix = new ExchangeMatrix(productIndex.size(),
				productIndex.size());
		interventionMatrix = new ExchangeMatrix(flowIndex.size(),
				productIndex.size());
		return createInventory();
	}

	private Inventory createInventory() {
		Inventory inventory = new Inventory();
		inventory.setAllocationMethod(allocationMethod);
		inventory.setFlowIndex(flowIndex);
		inventory.setInterventionMatrix(interventionMatrix);
		inventory.setProductIndex(productIndex);
		inventory.setTechnologyMatrix(technologyMatrix);
		fillMatrices();
		return inventory;
	}

	private void fillMatrices() {
		try {
			Map<Long, List<CalcExchange>> map = cache.getExchangeCache()
					.getAll(productIndex.getProcessIds());
			for (Long processId : productIndex.getProcessIds()) {
				List<CalcExchange> exchanges = map.get(processId);
				List<LongPair> processProducts = productIndex
						.getProducts(processId);
				for (LongPair processProduct : processProducts) {
					for (CalcExchange exchange : exchanges) {
						putExchangeValue(processProduct, exchange);
					}
				}
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to load exchanges from cache", e);
		}
	}

	private void putExchangeValue(LongPair processProduct, CalcExchange e) {
		if (!e.input && processProduct.equals(e.processId, e.flowId)) {
			// the reference product
			int idx = productIndex.getIndex(processProduct);
			add(idx, processProduct, technologyMatrix, e);

		} else if (e.flowType == FlowType.ELEMENTARY_FLOW) {
			// elementary exchanges
			addIntervention(processProduct, e);

		} else if (e.input) {

			LongPair inputProduct = new LongPair(e.processId, e.flowId);
			if (productIndex.isLinkedInput(inputProduct)) {
				// linked product inputs
				addProcessLink(processProduct, e, inputProduct);
			} else {
				// an unlinked product input
				addIntervention(processProduct, e);
			}

		} else if (allocationMethod == null
				|| allocationMethod == AllocationMethod.NONE) {
			// non allocated output products
			addIntervention(processProduct, e);
		}
	}

	private void addProcessLink(LongPair processProduct, CalcExchange e,
			LongPair inputProduct) {
		LongPair linkedOutput = productIndex.getLinkedOutput(inputProduct);
		int row = productIndex.getIndex(linkedOutput);
		add(row, processProduct, technologyMatrix, e);
	}

	private void addIntervention(LongPair processProduct, CalcExchange e) {
		int row = flowIndex.getIndex(e.flowId);
		add(row, processProduct, interventionMatrix, e);
	}

	private void add(int row, LongPair processProduct, ExchangeMatrix matrix,
			CalcExchange exchange) {
		int col = productIndex.getIndex(processProduct);
		if (row < 0 || col < 0)
			return;
		ExchangeCell existingCell = matrix.getEntry(row, col);
		if (existingCell != null) {
			// self loops or double entries
			exchange = mergeExchanges(existingCell, exchange);
		}
		ExchangeCell cell = new ExchangeCell(exchange);
		if (allocationTable != null) {
			// note that the allocation table assures that the factor is 1.0 for
			// reference products
			double factor = allocationTable.getFactor(processProduct, exchange);
			cell.setAllocationFactor(factor);
		}
		matrix.setEntry(row, col, cell);
	}

	private CalcExchange mergeExchanges(ExchangeCell existingCell,
			CalcExchange addExchange) {
		// a possible allocation factor is handled outside of this function
		CalcExchange exExchange = existingCell.exchange;
		double existingVal = getMergeValue(exExchange);
		double addVal = getMergeValue(addExchange);
		double val = existingVal + addVal;
		CalcExchange newExchange = new CalcExchange();
		newExchange.input = val < 0;
		newExchange.conversionFactor = 1;
		newExchange.flowId = addExchange.flowId;
		newExchange.flowType = addExchange.flowType;
		newExchange.processId = addExchange.processId;
		newExchange.amount = Math.abs(val);
		if (exExchange.amountFormula != null
				&& addExchange.amountFormula != null) {
			newExchange.amountFormula = getMergeFormula(exExchange)
					+ " + " + getMergeFormula(addExchange);
		}
		// TODO: adding up uncertainty information (with formulas!) is not yet
		// handled
		return newExchange;
	}

	private double getMergeValue(CalcExchange e) {
		double v = e.amount * e.conversionFactor;
		if (e.input && !e.avoidedProduct)
			return -v;
		else
			return v;
	}

	private String getMergeFormula(CalcExchange e) {
		String f;
		if (e.amountFormula == null)
			f = Double.toString(e.amount);
		else
			f = "(" + e.amountFormula + ")";
		if (e.conversionFactor != 1)
			f += " * " + e.conversionFactor;
		return f;
	}
}
