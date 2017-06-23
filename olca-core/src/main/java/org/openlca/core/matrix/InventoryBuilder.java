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
	private final TechIndex techIndex;
	private final AllocationMethod allocationMethod;

	private FlowIndex flowIndex;
	private AllocationIndex allocationIndex;
	private ExchangeMatrix technologyMatrix;
	private ExchangeMatrix interventionMatrix;

	InventoryBuilder(MatrixCache matrixCache, TechIndex productIndex,
			AllocationMethod allocationMethod) {
		this.cache = matrixCache;
		this.techIndex = productIndex;
		this.allocationMethod = allocationMethod;
	}

	Inventory build() {
		if (allocationMethod != null
				&& allocationMethod != AllocationMethod.NONE) {
			allocationIndex = AllocationIndex.create(techIndex,
					allocationMethod, cache);
		}
		flowIndex = FlowIndex.build(cache, techIndex, allocationMethod);
		technologyMatrix = new ExchangeMatrix(techIndex.size(),
				techIndex.size());
		interventionMatrix = new ExchangeMatrix(flowIndex.size(),
				techIndex.size());
		return createInventory();
	}

	private Inventory createInventory() {
		Inventory inventory = new Inventory();
		inventory.allocationMethod = allocationMethod;
		inventory.flowIndex = flowIndex;
		inventory.interventionMatrix = interventionMatrix;
		inventory.productIndex = techIndex;
		inventory.technologyMatrix = technologyMatrix;
		fillMatrices();
		return inventory;
	}

	private void fillMatrices() {
		try {
			Map<Long, List<CalcExchange>> map = cache.getExchangeCache()
					.getAll(techIndex.getProcessIds());
			for (Long processID : techIndex.getProcessIds()) {
				List<CalcExchange> exchanges = map.get(processID);
				List<LongPair> providers = techIndex.getProviders(processID);
				for (LongPair provider : providers) {
					for (CalcExchange exchange : exchanges) {
						putExchangeValue(provider, exchange);
					}
				}
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to load exchanges from cache", e);
		}
	}

	private void putExchangeValue(LongPair provider, CalcExchange e) {
		if (e.flowType == FlowType.ELEMENTARY_FLOW) {
			// elementary flows
			addIntervention(provider, e);
			return;
		}

		if ((e.isInput && e.flowType == FlowType.PRODUCT_FLOW)
				|| (!e.isInput && e.flowType == FlowType.WASTE_FLOW)) {
			if (techIndex.isLinked(LongPair.of(e.processId, e.exchangeId))) {
				// linked product input or waste output
				addProcessLink(provider, e);
			} else {
				// unlinked product input or waste output
				addIntervention(provider, e);
			}
			return;
		}

		if (provider.equals(e.processId, e.flowId)) {
			// the reference product or waste flow
			int idx = techIndex.getIndex(provider);
			add(idx, provider, technologyMatrix, e);
			return;
		}

		if (allocationMethod == null
				|| allocationMethod == AllocationMethod.NONE) {
			// non allocated output products or waste inputs
			addIntervention(provider, e);
		}
	}

	private void addProcessLink(LongPair processProduct, CalcExchange e) {
		LongPair exchange = LongPair.of(e.processId, e.exchangeId);
		LongPair provider = techIndex.getLinkedProvider(exchange);
		int row = techIndex.getIndex(provider);
		add(row, processProduct, technologyMatrix, e);
	}

	private void addIntervention(LongPair processProduct, CalcExchange e) {
		int row = flowIndex.getIndex(e.flowId);
		add(row, processProduct, interventionMatrix, e);
	}

	private void add(int row, LongPair processProduct, ExchangeMatrix matrix,
			CalcExchange exchange) {
		int col = techIndex.getIndex(processProduct);
		if (row < 0 || col < 0)
			return;
		ExchangeCell existingCell = matrix.getEntry(row, col);
		if (existingCell != null) {
			// self loops or double entries
			exchange = mergeExchanges(existingCell, exchange);
		}
		ExchangeCell cell = new ExchangeCell(exchange);
		if (allocationIndex != null) {
			// note that the allocation table assures that the factor is 1.0 for
			// reference products
			double factor = allocationIndex.getFactor(processProduct, exchange);
			cell.allocationFactor = factor;
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
		newExchange.isInput = val < 0;
		newExchange.conversionFactor = 1;
		newExchange.flowId = addExchange.flowId;
		newExchange.flowType = addExchange.flowType;
		newExchange.processId = addExchange.processId;
		newExchange.amount = Math.abs(val);
		if (exExchange.amountFormula != null
				&& addExchange.amountFormula != null) {
			newExchange.amountFormula = "abs( " + getMergeFormula(exExchange)
					+ " + " + getMergeFormula(addExchange) + ")";
		}
		newExchange.costValue = getMergeCosts(exExchange, addExchange);
		// TODO: adding up uncertainty information (with formulas!) is not yet
		// handled
		return newExchange;
	}

	private double getMergeValue(CalcExchange e) {
		double v = e.amount * e.conversionFactor;
		if (e.isInput && !e.isAvoided)
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
		if (e.isInput && !e.isAvoided)
			f = "( -1 * (" + f + "))";
		return f;
	}

	private double getMergeCosts(CalcExchange e1, CalcExchange e2) {
		if (e1.costValue == 0)
			return e2.costValue;
		if (e2.costValue == 0)
			return e1.costValue;
		// TODO: this would be rarely the case but if the same flow in a single
		// process is given in different currencies with different conversion
		// the following would be not correct.
		double v1 = e1.isInput ? e1.costValue : -e1.costValue;
		double v2 = e2.isInput ? e2.costValue : -e2.costValue;
		// TODO: cost formulas
		return Math.abs(v1 + v2);
	}
}
