package org.openlca.core.matrices;

import java.util.List;

import org.openlca.core.indices.CalcExchange;
import org.openlca.core.indices.ExchangeTable;
import org.openlca.core.indices.FlowIndex;
import org.openlca.core.indices.LongPair;
import org.openlca.core.indices.ProductIndex;
import org.openlca.core.math.IMatrix;
import org.openlca.core.math.MatrixFactory;
import org.openlca.core.model.FlowType;

public class InventoryMatrixBuilder {

	private ProductIndex productIndex;
	private FlowIndex flowIndex;
	private ExchangeTable exchangeTable;

	private IMatrix technologyMatrix;
	private IMatrix interventionMatrix;

	public InventoryMatrixBuilder(ProductIndex productIndex,
			FlowIndex flowIndex, ExchangeTable exchangeTable) {
		this.productIndex = productIndex;
		this.flowIndex = flowIndex;
		this.exchangeTable = exchangeTable;
	}

	public InventoryMatrix build() {
		// TODO: if product index of flow index is empty, create an empty matrix
		// that will produce no results in the calculation
		technologyMatrix = MatrixFactory.create(productIndex.size(),
				productIndex.size());
		interventionMatrix = MatrixFactory.create(flowIndex.size(),
				productIndex.size());
		InventoryMatrix inventoryMatrix = new InventoryMatrix();
		inventoryMatrix.setFlowIndex(flowIndex);
		inventoryMatrix.setInterventionMatrix(interventionMatrix);
		inventoryMatrix.setProductIndex(productIndex);
		inventoryMatrix.setTechnologyMatrix(technologyMatrix);
		fillMatrices();
		return inventoryMatrix;
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
		if (processProduct.equals(e.getProcessId(), e.getFlowId())) {
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

	private void add(int row, int col, IMatrix matrix, CalcExchange exchange) {
		if (row < 0 || col < 0)
			return;
		double val = exchange.isInput() ? -exchange.getAmount() : exchange
				.getAmount();
		double old = technologyMatrix.getEntry(row, col);
		technologyMatrix.setEntry(row, col, old + val);
	}

}
