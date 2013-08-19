package org.openlca.core.matrices;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;

public class InventoryBuilder {

	private IDatabase database;
	private ProductIndex productIndex;
	private FlowIndex flowIndex;
	private ExchangeTable exchangeTable;
	private AllocationTable allocationTable;
	private AllocationMethod allocationMethod;

	private ExchangeMatrix technologyMatrix;
	private ExchangeMatrix interventionMatrix;

	public InventoryBuilder(IDatabase database) {
		this.database = database;
	}

	public Inventory build(ProductIndex productIndex,
			AllocationMethod allocationMethod) {
		this.productIndex = productIndex;
		this.allocationMethod = allocationMethod;
		if (allocationMethod != null
				&& allocationMethod != AllocationMethod.NONE)
			allocationTable = new AllocationTable(database, productIndex,
					allocationMethod);
		exchangeTable = new ExchangeTable(database,
				productIndex.getProcessIds());
		flowIndex = new FlowIndex(productIndex, exchangeTable, allocationMethod);
		technologyMatrix = new ExchangeMatrix(productIndex.size(),
				productIndex.size());
		interventionMatrix = new ExchangeMatrix(flowIndex.size(),
				productIndex.size());
		return createInventory(productIndex, allocationMethod);
	}

	private Inventory createInventory(ProductIndex productIndex,
			AllocationMethod allocationMethod) {
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
		for (Long processId : exchangeTable.getProcessIds()) {
			List<CalcExchange> exchanges = exchangeTable
					.getExchanges(processId);
			List<LongPair> processProducts = productIndex
					.getProducts(processId);
			for (LongPair processProduct : processProducts) {
				for (CalcExchange exchange : exchanges) {
					putExchangeValue(processProduct, exchange);
				}
			}
		}
	}

	private void putExchangeValue(LongPair processProduct, CalcExchange e) {
		if (!e.isInput()
				&& processProduct.equals(e.getProcessId(), e.getFlowId())) {
			// the reference product
			int idx = productIndex.getIndex(processProduct);
			add(idx, processProduct, technologyMatrix, e);

		} else if (e.getFlowType() == FlowType.ELEMENTARY_FLOW) {
			// elementary exchanges
			addIntervention(processProduct, e);

		} else if (e.isInput()) {

			LongPair inputProduct = new LongPair(e.getProcessId(),
					e.getFlowId());

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
		int row = flowIndex.getIndex(e.getFlowId());
		add(row, processProduct, interventionMatrix, e);
	}

	private void add(int row, LongPair processProduct, ExchangeMatrix matrix,
			CalcExchange exchange) {
		int col = productIndex.getIndex(processProduct);
		if (row < 0 || col < 0)
			return;
		ExchangeCell cell = new ExchangeCell(exchange);
		if (allocationTable != null) {
			// note that the allocation table assures that the factor is 1.0 for
			// reference products
			double factor = allocationTable.getFactor(processProduct, exchange);
			cell.setAllocationFactor(factor);
		}
		matrix.setEntry(row, col, cell);
	}
}
