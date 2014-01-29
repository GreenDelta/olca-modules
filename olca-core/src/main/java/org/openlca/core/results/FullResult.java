package org.openlca.core.results;

import org.openlca.core.math.IMatrix;
import org.openlca.core.matrix.LongPair;

public class FullResult extends ContributionResult implements IFullResult {

	protected IMatrix upstreamFlowResults;
	protected IMatrix upstreamImpactResults;

	public void setUpstreamFlowResults(IMatrix upstreamFlowResults) {
		this.upstreamFlowResults = upstreamFlowResults;
	}

	@Override
	public IMatrix getUpstreamFlowResults() {
		return upstreamFlowResults;
	}

	@Override
	public double getUpstreamFlowResult(LongPair processProduct, long flowId) {
		int row = flowIndex.getIndex(flowId);
		int col = productIndex.getIndex(processProduct);
		return getValue(upstreamFlowResults, row, col);
	}

	@Override
	public double getUpstreamFlowResult(long processId, long flowId) {
		int row = flowIndex.getIndex(flowId);
		return getProcessValue(upstreamFlowResults, row, processId);
	}

	public void setUpstreamImpactResults(IMatrix upstreamImpactResults) {
		this.upstreamImpactResults = upstreamImpactResults;
	}

	@Override
	public IMatrix getUpstreamImpactResults() {
		return upstreamImpactResults;
	}

	@Override
	public double getUpstreamImpactResult(LongPair processProduct, long impactId) {
		if (!hasImpactResults())
			return 0;
		int row = impactIndex.getIndex(impactId);
		int col = productIndex.getIndex(processProduct);
		return getValue(upstreamImpactResults, row, col);
	}

	@Override
	public double getUpstreamImpactResult(long processId, long impactId) {
		if (!hasImpactResults())
			return 0;
		int row = impactIndex.getIndex(impactId);
		return getProcessValue(upstreamImpactResults, row, processId);
	}

}
