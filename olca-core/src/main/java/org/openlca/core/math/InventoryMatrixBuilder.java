package org.openlca.core.math;

import java.util.List;

import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;

/**
 * Builds the inventory matrix for standard LCA calculations.
 */
public class InventoryMatrixBuilder {

	private ProductSystem productSystem;

	private IMatrix technologyMatrix;
	private IMatrix interventionMatrix;
	private FlowIndex flowIndex;
	private ProductIndex productIndex;

	public InventoryMatrixBuilder(ProductSystem productSystem) {
		this.productSystem = productSystem;
	}

	public InventoryMatrix build() {
		productIndex = new ProductIndex(productSystem);
		flowIndex = new FlowIndex();
		technologyMatrix = MatrixFactory.create(productIndex.size(),
				productIndex.size());
		prepareInterventionMatrix();
		fillMatrices();
		InventoryMatrix matrix = new InventoryMatrix();
		matrix.setFlowIndex(flowIndex);
		matrix.setInterventionMatrix(interventionMatrix);
		matrix.setProductIndex(productIndex);
		matrix.setTechnologyMatrix(technologyMatrix);
		return matrix;
	}

	private void prepareInterventionMatrix() {
		for (Process process : productSystem.getProcesses()) {
			for (Exchange exchange : process.getExchanges()) {
				if (productIndex.contains(process, exchange)
						|| productIndex.isLinkedInput(exchange)
						|| flowIndex.contains(exchange.getFlow()))
					continue;
				// if (exchange is elementary or product input which is not
				// linked or parent process is not allocated) and if its a
				// product input its not linked
				if ((exchange.getFlow().getFlowType() == FlowType.ElementaryFlow
						|| exchange.isInput()
						|| process.getAllocationMethod() == AllocationMethod.None || process
							.getAllocationMethod() == null)) {
					flowIndex.put(exchange.getFlow());
					flowIndex.setInput(exchange.getFlow(), exchange.isInput());
				}
			}
		}
		// create the intervention matrix
		if (flowIndex.size() > 0 && productIndex.size() > 0) {
			interventionMatrix = MatrixFactory.create(flowIndex.size(),
					productIndex.size());
		}
	}

	private void fillMatrices() {
		for (Process process : productSystem.getProcesses()) {
			for (Exchange exchange : process.getExchanges()) {
				if (productIndex.contains(process, exchange)) {
					makeTechnologyMatrixEntry(process, exchange);
				} else if (productIndex.isLinkedInput(exchange)) {
					makeTechnologyMatrixEntries(process, exchange);
				} else if (flowIndex.contains(exchange.getFlow())) {
					makeInterventionMatrixEntries(process, exchange);
				}
			}
		}
	}

	private void makeInterventionMatrixEntries(Process process,
			Exchange exchange) {
		int row = flowIndex.getIndex(exchange.getFlow());
		List<String> processProducts = productIndex.getProducts(process);
		for (String productId : processProducts) {
			int col = productIndex.getIndex(productId);
			if (row < 0 || col < 0) // allocated outputs
				continue;
			double oldValue = interventionMatrix.getEntry(row, col);
			double resultingAmount = exchange.getConvertedResult();
			if (exchange.isInput()) {
				resultingAmount *= -1;
			}
			interventionMatrix.setEntry(row, col, oldValue + resultingAmount);
		}
	}

	private void makeTechnologyMatrixEntries(Process process, Exchange input) {
		List<String> processProducts = productIndex.getProducts(process);
		String outputKey = productIndex.getLinkedOutputKey(input);
		int row = productIndex.getIndex(outputKey);
		for (String productId : processProducts) {
			int col = productIndex.getIndex(productId);
			double oldValue = technologyMatrix.getEntry(row, col);
			double newValue = -1 * input.getConvertedResult();
			if (input.isAvoidedProduct()) {
				newValue *= -1;
			}
			technologyMatrix.setEntry(row, col, oldValue + newValue);
		}
	}

	private void makeTechnologyMatrixEntry(Process process, Exchange output) {
		Integer idx = productIndex.getIndex(process, output);
		double oldValue = technologyMatrix.getEntry(idx, idx);
		double newValue = oldValue + output.getConvertedResult();
		technologyMatrix.setEntry(idx, idx, newValue);
	}

}
