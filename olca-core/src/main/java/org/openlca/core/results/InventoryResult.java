package org.openlca.core.results;

import org.openlca.core.matrices.FlowIndex;
import org.openlca.core.matrices.LongIndex;
import org.openlca.core.matrices.LongPair;
import org.openlca.core.matrices.ProductIndex;

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

	public void setProductIndex(ProductIndex productIndex) {
		this.productIndex = productIndex;
	}

	public void setFlowIndex(FlowIndex flowIndex) {
		this.flowIndex = flowIndex;
	}

	public void setImpactIndex(LongIndex impactIndex) {
		this.impactIndex = impactIndex;
	}

	public void setFlowResults(double[] flowResults) {
		this.flowResults = flowResults;
	}

	public void setImpactResults(double[] impactResults) {
		this.impactResults = impactResults;
	}

	public void setScalingFactors(double[] scalingFactors) {
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
		if (flowResults == null)
			return new double[0];
		double[] vals = new double[flowResults.length];
		for (int i = 0; i < vals.length; i++) {
			long flowId = flowIndex.getFlowAt(i);
			vals[i] = adoptFlowResult(flowResults[i], flowId);
		}
		return vals;
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
		return adoptFlowResult(flowResults[idx], flowId);
	}

	private double adoptFlowResult(double value, long flowId) {
		if (value == 0)
			return 0; // avoid -0 in the results
		boolean inputFlow = flowIndex.isInput(flowId);
		return inputFlow ? -value : value;
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
