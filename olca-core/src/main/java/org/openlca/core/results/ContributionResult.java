package org.openlca.core.results;

import java.util.List;

import org.openlca.core.math.IMatrix;
import org.openlca.core.matrix.LongPair;

public class ContributionResult extends BaseResult implements
		IContributionResult {

	protected double[] scalingFactors;
	protected IMatrix singleFlowResults;
	protected IMatrix singleImpactResults;
	protected IMatrix singleFlowImpacts;
	protected LinkContributions linkContributions;

	public void setScalingFactors(double[] scalingFactors) {
		this.scalingFactors = scalingFactors;
	}

	@Override
	public double[] getScalingFactors() {
		return scalingFactors;
	}

	@Override
	public double getScalingFactor(LongPair processProduct) {
		int idx = productIndex.getIndex(processProduct);
		if (idx < 0 || idx > scalingFactors.length)
			return 0;
		return scalingFactors[idx];
	}

	@Override
	public double getScalingFactor(long processId) {
		double factor = 0;
		List<LongPair> productIds = productIndex.getProducts(processId);
		for (LongPair product : productIds) {
			int idx = productIndex.getIndex(product);
			if (idx < 0 || idx > scalingFactors.length)
				continue;
			factor += scalingFactors[idx];
		}
		return factor;
	}

	public void setSingleFlowResults(IMatrix singleFlowResults) {
		this.singleFlowResults = singleFlowResults;
	}

	@Override
	public IMatrix getSingleFlowResults() {
		return singleFlowResults;
	}

	@Override
	public double getSingleFlowResult(LongPair processProduct, long flowId) {
		int row = flowIndex.getIndex(flowId);
		int col = productIndex.getIndex(processProduct);
		return getValue(singleFlowResults, row, col);
	}

	@Override
	public double getSingleFlowResult(long processId, long flowId) {
		int row = flowIndex.getIndex(flowId);
		return getProcessValue(singleFlowResults, row, processId);
	}

	public void setSingleImpactResults(IMatrix singleImpactResults) {
		this.singleImpactResults = singleImpactResults;
	}

	@Override
	public IMatrix getSingleImpactResults() {
		return singleImpactResults;
	}

	@Override
	public double getSingleImpactResult(LongPair processProduct, long impactId) {
		if (!hasImpactResults())
			return 0;
		int row = impactIndex.getIndex(impactId);
		int col = productIndex.getIndex(processProduct);
		return getValue(singleImpactResults, row, col);
	}

	@Override
	public double getSingleImpactResult(long processId, long impactId) {
		if (!hasImpactResults())
			return 0;
		int row = impactIndex.getIndex(impactId);
		return getProcessValue(singleImpactResults, row, processId);
	}

	public void setSingleFlowImpacts(IMatrix singleFlowImpacts) {
		this.singleFlowImpacts = singleFlowImpacts;
	}

	@Override
	public IMatrix getSingleFlowImpacts() {
		return singleFlowImpacts;
	}

	@Override
	public double getSingleFlowImpact(long flowId, long impactId) {
		if (!hasImpactResults())
			return 0;
		int row = impactIndex.getIndex(impactId);
		int col = flowIndex.getIndex(flowId);
		return getValue(singleFlowImpacts, row, col);
	}

	public void setLinkContributions(LinkContributions linkContributions) {
		this.linkContributions = linkContributions;
	}

	@Override
	public LinkContributions getLinkContributions() {
		return linkContributions;
	}

	protected double getProcessValue(IMatrix matrix, int row, long processId) {
		if (matrix == null)
			return 0;
		double colSum = 0;
		for (LongPair product : productIndex.getProducts(processId)) {
			int col = productIndex.getIndex(product);
			colSum += getValue(matrix, row, col);
		}
		return colSum;
	}

	protected double getValue(IMatrix matrix, int row, int col) {
		if (matrix == null)
			return 0d;
		if (row < 0 || row >= matrix.getRowDimension())
			return 0d;
		if (col < 0 || col >= matrix.getColumnDimension())
			return 0d;
		return matrix.getEntry(row, col);
	}

}
