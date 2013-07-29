package org.openlca.core.matrices;

import org.openlca.core.indices.FlowIndex;
import org.openlca.core.indices.LongIndex;
import org.openlca.core.indices.LongPair;
import org.openlca.core.indices.ProductIndex;

/**
 * The result type of a normal inventory calculation. The vector flowResults is
 * the vector g in the matrix method. The impact assessment results are derived
 * by F * g where F is the matrix with the impact assessment factors. Impact
 * assessment results are optional (means they are null when the result was
 * calculated without an impact assessment method).
 * 
 * TODO: we could easily add flow contributions to impact categories via the
 * formula C = F * diag(g).
 * 
 */
public class InventoryResult {

	private ProductIndex productIndex;
	private FlowIndex flowIndex;
	private LongIndex impactIndex;
	private double[] flowResults;
	private double[] impactResults;
	private double[] scalingFactors;

	void setProductIndex(ProductIndex productIndex) {
		this.productIndex = productIndex;
	}

	void setFlowIndex(FlowIndex flowIndex) {
		this.flowIndex = flowIndex;
	}

	void setImpactIndex(LongIndex impactIndex) {
		this.impactIndex = impactIndex;
	}

	void setFlowResults(double[] flowResults) {
		this.flowResults = flowResults;
	}

	void setImpactResults(double[] impactResults) {
		this.impactResults = impactResults;
	}

	void setScalingFactors(double[] scalingFactors) {
		this.scalingFactors = scalingFactors;
	}

	public ProductIndex getProductIndex() {
		return productIndex;
	}

	public FlowIndex getFlowIndex() {
		return flowIndex;
	}

	public LongIndex getImpactIndex() {
		return impactIndex;
	}

	public double[] getFlowResults() {
		return flowResults;
	}

	public double[] getImpactResults() {
		return impactResults;
	}

	public double[] getScalingFactors() {
		return scalingFactors;
	}

	public double getFlowResult(long flowId) {
		int idx = flowIndex.getIndex(flowId);
		if (idx < 0 || idx >= flowResults.length)
			return 0;
		return flowResults[idx];
	}

	public boolean hasImpactResults() {
		return impactIndex != null && !impactIndex.isEmpty()
				&& impactResults != null && impactResults.length > 0;
	}

	public double getScalingFactor(LongPair processProduct) {
		int idx = productIndex.getIndex(processProduct);
		if (idx < 0 || idx >= scalingFactors.length)
			return 0;
		return scalingFactors[idx];
	}

	public double getImpactResult(long impactCategoryId) {
		if (!hasImpactResults())
			return 0;
		int idx = impactIndex.getIndex(impactCategoryId);
		if (idx < 0 || idx >= impactResults.length)
			return 0;
		return impactResults[idx];
	}

}
