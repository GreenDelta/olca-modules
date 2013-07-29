package org.openlca.core.matrices;

import org.openlca.core.indices.FlowIndex;
import org.openlca.core.indices.ProductIndex;

public class Inventory {

	private ProductIndex productIndex;
	private FlowIndex flowIndex;
	private ExchangeMatrix technologyMatrix;
	private ExchangeMatrix interventionMatrix;

	public boolean isEmpty() {
		return productIndex == null || productIndex.size() == 0
				|| flowIndex == null || flowIndex.isEmpty()
				|| technologyMatrix == null || technologyMatrix.isEmpty()
				|| interventionMatrix == null || interventionMatrix.isEmpty();
	}

	public ProductIndex getProductIndex() {
		return productIndex;
	}

	public void setProductIndex(ProductIndex productIndex) {
		this.productIndex = productIndex;
	}

	public FlowIndex getFlowIndex() {
		return flowIndex;
	}

	public void setFlowIndex(FlowIndex flowIndex) {
		this.flowIndex = flowIndex;
	}

	public ExchangeMatrix getTechnologyMatrix() {
		return technologyMatrix;
	}

	public void setTechnologyMatrix(ExchangeMatrix technologyMatrix) {
		this.technologyMatrix = technologyMatrix;
	}

	public ExchangeMatrix getInterventionMatrix() {
		return interventionMatrix;
	}

	public void setInterventionMatrix(ExchangeMatrix interventionMatrix) {
		this.interventionMatrix = interventionMatrix;
	}

}
