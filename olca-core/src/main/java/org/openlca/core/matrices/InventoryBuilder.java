package org.openlca.core.matrices;

import java.util.List;

import org.openlca.core.indices.CalcExchange;
import org.openlca.core.indices.ExchangeTable;
import org.openlca.core.indices.FlowIndex;
import org.openlca.core.indices.LongPair;
import org.openlca.core.indices.ProductIndex;
import org.openlca.core.model.FlowType;

public class InventoryBuilder {

	private ProductIndex productIndex;
	private FlowIndex flowIndex;
	private ExchangeTable exchangeTable;

	private ExchangeMatrix technologyMatrix;
	private ExchangeMatrix interventionMatrix;

	public InventoryBuilder(ProductIndex productIndex, FlowIndex flowIndex,
			ExchangeTable exchangeTable) {
		this.productIndex = productIndex;
		this.flowIndex = flowIndex;
		this.exchangeTable = exchangeTable;
	}

	public Inventory build() {
		technologyMatrix = new ExchangeMatrix(productIndex.size(),
				productIndex.size());
		interventionMatrix = new ExchangeMatrix(flowIndex.size(),
				productIndex.size());
		Inventory inventory = new Inventory();
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
			int idx = productIndex.getIndex(processProduct);
			add(idx, idx, technologyMatrix, e);
		} else if (e.getFlowType() == FlowType.ELEMENTARY_FLOW) {
			addIntervention(processProduct, e);
		} else if (e.isInput()) {
			LongPair inputProduct = new LongPair(e.getProcessId(),
					e.getFlowId());
			if (productIndex.isLinkedInput(inputProduct))
				addProcessLink(processProduct, e, inputProduct);
			else
				addIntervention(processProduct, e);
		}
		// TODO: non-allocated output-products
	}

	private void addProcessLink(LongPair processProduct, CalcExchange e,
			LongPair inputProduct) {
		LongPair linkedOutput = productIndex.getLinkedOutput(inputProduct);
		int row = productIndex.getIndex(linkedOutput);
		int col = productIndex.getIndex(processProduct);
		add(row, col, technologyMatrix, e);
	}

	private void addIntervention(LongPair processProduct, CalcExchange e) {
		int row = flowIndex.getIndex(e.getFlowId());
		int col = productIndex.getIndex(processProduct);
		add(row, col, interventionMatrix, e);
	}

	private void add(int row, int col, ExchangeMatrix matrix,
			CalcExchange exchange) {
		if (row < 0 || col < 0)
			return;
		matrix.setEntry(row, col, new ExchangeCell(exchange));
	}

}
