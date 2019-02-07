package org.openlca.core.matrix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
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

	InventoryBuilder(MatrixCache matrixCache, TechIndex techIndex,
			AllocationMethod allocationMethod) {
		this.cache = matrixCache;
		this.techIndex = techIndex;
		this.allocationMethod = allocationMethod;
	}

	Inventory build() {
		if (allocationMethod != null
				&& allocationMethod != AllocationMethod.NONE) {
			allocationIndex = AllocationIndex.create(
					cache.getDatabase(), techIndex, allocationMethod);
		}
		flowIndex = FlowIndex.build(cache, techIndex, allocationMethod);
		technologyMatrix = new ExchangeMatrix(techIndex.size(),
				techIndex.size());
		interventionMatrix = new ExchangeMatrix(flowIndex.size(),
				techIndex.size());
		return createInventory();
	}

	private Inventory createInventory() {
		Inventory inv = new Inventory();
		inv.allocationMethod = allocationMethod;
		inv.flowIndex = flowIndex;
		inv.interventionMatrix = interventionMatrix;
		inv.techIndex = techIndex;
		inv.technologyMatrix = technologyMatrix;
		fillMatrices();
		return inv;
	}

	private void fillMatrices() {
		try {

			// TODO: the cache loader throws an exception when we ask for
			// process IDs that do not exist; this we have to filter out
			// product system IDs here; see also FlowIndex
			HashSet<Long> processIds = new HashSet<>();
			ArrayList<ProcessProduct> systemLinks = new ArrayList<>();
			techIndex.each((i, p) -> {
				if (p.process == null)
					return;
				if (p.process.type == ModelType.PROCESS) {
					processIds.add(p.process.id);
				} else {
					systemLinks.add(p);
				}
			});

			Map<Long, List<CalcExchange>> map = cache.getExchangeCache()
					.getAll(processIds);

			for (Long processID : techIndex.getProcessIds()) {
				List<CalcExchange> exchanges = map.get(processID);
				if (exchanges == null)
					continue;
				List<ProcessProduct> providers = techIndex
						.getProviders(processID);
				for (ProcessProduct provider : providers) {
					for (CalcExchange exchange : exchanges) {
						putExchangeValue(provider, exchange);
					}
				}
			}

			// now put the entries of the sub-system links into the technosphere
			// matrix
			for (ProcessProduct sysLink : systemLinks) {

				// select the ID of the reference exchange
				String query = "select f_reference_exchange from "
						+ "tbl_product_systems where id = "
						+ sysLink.process.id;
				AtomicLong qref = new AtomicLong();
				NativeSql.on(cache.getDatabase()).query(query, r -> {
					qref.set(r.getLong(1));
					return false;
				});

				// select the process+flow of that exchange
				AtomicReference<ProcessProduct> procRef = new AtomicReference<>();
				query = "select f_owner, f_flow from tbl_exchanges where id = "
						+ qref.get();
				NativeSql.on(cache.getDatabase()).query(query, r -> {
					long processId = r.getLong(1);
					long flowId = r.getLong(2);
					ProcessProduct p = techIndex.getProvider(processId, flowId);
					procRef.set(p);
					return false;
				});

				ProcessProduct procLink = procRef.get();
				if (procLink == null) {
					// TODO: log this as an error
					continue;
				}

				int sysIdx = techIndex.getIndex(sysLink);
				int procIdx = techIndex.getIndex(procLink);
				if (sysIdx < 0 || procIdx < 0) {
					// TODO: log this as an error
					continue;
				}

				ExchangeCell procCell = technologyMatrix.getEntry(
						procIdx, procIdx);
				if (procCell == null || procCell.exchange == null) {
					// TODO: log this as an error
					continue;
				}
				double val = procCell.getMatrixValue();
				if (procCell.exchange.isInput) {
					val = -val;
				}

				CalcExchange sysEx = new CalcExchange();
				sysEx.isInput = procCell.exchange.isInput;
				sysEx.conversionFactor = 1.0;
				sysEx.amount = val;
				technologyMatrix.setEntry(
						sysIdx, sysIdx, new ExchangeCell(sysEx));

				CalcExchange procEx = new CalcExchange();
				procEx.isInput = !procCell.exchange.isInput;
				procEx.conversionFactor = 1.0;
				procEx.amount = val;
				technologyMatrix.setEntry(
						procIdx, sysIdx, new ExchangeCell(procEx));
			}

		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to load exchanges from cache", e);
		}
	}

	private void putExchangeValue(ProcessProduct provider, CalcExchange e) {
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

	private void addProcessLink(ProcessProduct processProduct, CalcExchange e) {
		LongPair exchange = LongPair.of(e.processId, e.exchangeId);
		ProcessProduct provider = techIndex.getLinkedProvider(exchange);
		int row = techIndex.getIndex(provider);
		add(row, processProduct, technologyMatrix, e);
	}

	private void addIntervention(ProcessProduct provider, CalcExchange e) {
		int row = flowIndex.of(e.flowId);
		add(row, provider, interventionMatrix, e);
	}

	private void add(int row, ProcessProduct provider, ExchangeMatrix matrix,
			CalcExchange exchange) {
		int col = techIndex.getIndex(provider);
		if (row < 0 || col < 0)
			return;
		ExchangeCell existingCell = matrix.getEntry(row, col);
		if (existingCell != null) {
			// self loops or double entries
			exchange = mergeExchanges(existingCell, exchange);
		}
		ExchangeCell cell = new ExchangeCell(exchange);
		if (allocationIndex != null && exchange.isAllocatable()) {
			cell.allocationFactor = allocationIndex.get(
					provider, exchange.exchangeId);
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
