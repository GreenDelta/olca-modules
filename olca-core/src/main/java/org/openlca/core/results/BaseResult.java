package org.openlca.core.results;

import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.LongIndex;
import org.openlca.core.matrix.ProductIndex;

/**
 * A base result is the simplest kind of result of a calculated product system.
 * It contains the total interventions and impact assessment results of the
 * product system.
 */
public class BaseResult implements IBaseResult {

	protected ProductIndex productIndex;
	protected FlowIndex flowIndex;
	protected LongIndex impactIndex;
	protected double[] totalFlowResults;
	protected double[] totalImpactResults;

	public void setProductIndex(ProductIndex productIndex) {
		this.productIndex = productIndex;
	}

	@Override
	public ProductIndex getProductIndex() {
		return productIndex;
	}

	public void setFlowIndex(FlowIndex flowIndex) {
		this.flowIndex = flowIndex;
	}

	@Override
	public FlowIndex getFlowIndex() {
		return flowIndex;
	}

	public void setImpactIndex(LongIndex impactIndex) {
		this.impactIndex = impactIndex;
	}

	@Override
	public LongIndex getImpactIndex() {
		return impactIndex;
	}

	@Override
	public boolean hasImpactResults() {
		return impactIndex != null && totalImpactResults != null
				&& !impactIndex.isEmpty();
	}

	public void setTotalFlowResults(double[] totalFlowResults) {
		this.totalFlowResults = totalFlowResults;
	}

	@Override
	public double[] getTotalFlowResults() {
		return totalFlowResults;
	}

	@Override
	public double getTotalFlowResult(long flowId) {
		int idx = flowIndex.getIndex(flowId);
		if (idx < 0 || idx >= totalFlowResults.length)
			return 0;
		return totalFlowResults[idx];
	}

	public void setTotalImpactResults(double[] totalImpactResults) {
		this.totalImpactResults = totalImpactResults;
	}

	@Override
	public double[] getTotalImpactResults() {
		return totalImpactResults;
	}

	@Override
	public double getTotalImpactResult(long impactId) {
		if (!hasImpactResults())
			return 0;
		int idx = impactIndex.getIndex(impactId);
		if (idx < 0 || idx >= totalImpactResults.length)
			return 0;
		return totalImpactResults[idx];
	}

}
