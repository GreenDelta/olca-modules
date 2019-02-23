package org.openlca.core.matrix;

import java.util.HashSet;
import java.util.List;

import org.openlca.core.matrix.cache.ExchangeTable;
import org.openlca.core.matrix.cache.FlowTable;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.results.SimpleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryBuilder2 {

	private final InventoryConfig conf;
	private final TechIndex techIndex;
	private final FlowTable flows;

	private FlowIndex flowIndex;
	private AllocationIndex allocationIndex;
	private ExchangeMatrix technologyMatrix;
	private ExchangeMatrix interventionMatrix;

	// TODO: this will go into the matrix data
	private UMatrix techUncerts;
	private UMatrix enviUncerts;

	public InventoryBuilder2(InventoryConfig conf) {
		this.conf = conf;
		this.techIndex = conf.techIndex;
		this.flows = FlowTable.create(conf.db);
		if (conf.withUncertainties) {
			techUncerts = new UMatrix();
			enviUncerts = new UMatrix();
		}
	}

	public Inventory build() {
		if (conf.allocationMethod != null
				&& conf.allocationMethod != AllocationMethod.NONE) {
			allocationIndex = AllocationIndex.create(
					conf.db, techIndex, conf.allocationMethod);
		}

		// create the index of elementary flows; when the system has sub-systems
		// we add the flows of the sub-systems to the index; note that there
		// can be elementary flows that only occur in a sub-system
		flowIndex = new FlowIndex();
		if (conf.subResults != null) {
			for (SimpleResult sub : conf.subResults.values()) {
				if (sub.flowIndex == null)
					continue;
				sub.flowIndex.each(f -> {
					if (!flowIndex.contains(f)) {
						if (sub.isInput(f)) {
							flowIndex.putInput(f);
						} else {
							flowIndex.putOutput(f);
						}
					}
				});
			}
		}

		// allocate and fill the matrices
		technologyMatrix = new ExchangeMatrix(techIndex.size(),
				techIndex.size());
		interventionMatrix = new ExchangeMatrix(flowIndex.size(),
				techIndex.size());
		fillMatrices();

		// return the inventory
		Inventory inv = new Inventory();
		inv.allocationMethod = conf.allocationMethod;
		inv.flowIndex = flowIndex;
		inv.interventionMatrix = interventionMatrix;
		inv.techIndex = techIndex;
		inv.technologyMatrix = technologyMatrix;
		return inv;
	}

	private void fillMatrices() {
		try {
			// fill the matrices with process data
			ExchangeTable exchanges = new ExchangeTable(conf.db);
			exchanges.each(techIndex, exchange -> {
				List<ProcessProduct> products = techIndex
						.getProviders(exchange.processId);
				for (ProcessProduct product : products) {
					putExchangeValue(product, exchange);
				}
			});

			// now put the entries of the sub-system into the matrices
			HashSet<ProcessProduct> subSystems = new HashSet<>();
			techIndex.each((i, p) -> {
				if (p.process == null)
					return;
				if (p.process.type == ModelType.PRODUCT_SYSTEM) {
					subSystems.add(p);
				}
			});
			if (subSystems.isEmpty())
				return;
			for (ProcessProduct sub : subSystems) {

				int col = techIndex.getIndex(sub);
				SimpleResult r = conf.subResults.get(sub);

				// add the link in the technology matrix
				CalcExchange e = new CalcExchange();
				ExchangeCell techCell = new ExchangeCell(e);
				technologyMatrix.setEntry(col, col, techCell);
				e.conversionFactor = 1.0;
				e.flowId = sub.flowId();
				e.isInput = sub.flow.flowType == FlowType.WASTE_FLOW;
				if (r == null) {
					e.amount = 1.0;
					continue;
				}
				e.amount = r.techIndex.getDemand();

				// add the LCI result
				r.flowIndex.each(f -> {
					CalcExchange ee = new CalcExchange();
					ee.amount = r.getTotalFlowResult(f);
					ee.conversionFactor = 1.0;
					ee.flowId = f.id;
					ee.isInput = r.isInput(f);
					interventionMatrix.setEntry(
							flowIndex.of(f), col, new ExchangeCell(ee));
				});
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

		if (conf.allocationMethod == null
				|| conf.allocationMethod == AllocationMethod.NONE) {
			// non allocated output products or waste inputs
			addIntervention(provider, e);
		}
	}

	private void addProcessLink(ProcessProduct product, CalcExchange e) {
		LongPair exchange = LongPair.of(e.processId, e.exchangeId);
		ProcessProduct provider = techIndex.getLinkedProvider(exchange);
		int row = techIndex.getIndex(provider);
		add(row, product, technologyMatrix, e);
	}

	private void addIntervention(ProcessProduct provider, CalcExchange e) {
		int row = flowIndex.of(e.flowId);
		if (row < 0) {
			if (e.isInput) {
				flowIndex.putInput(flows.get(e.flowId));
			} else {
				flowIndex.putOutput(flows.get(e.flowId));
			}
		}
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
