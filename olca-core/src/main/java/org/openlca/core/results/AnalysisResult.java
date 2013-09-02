package org.openlca.core.results;

import java.util.List;

import org.openlca.core.math.IMatrix;
import org.openlca.core.matrices.FlowIndex;
import org.openlca.core.matrices.LongIndex;
import org.openlca.core.matrices.LongPair;
import org.openlca.core.matrices.ProductIndex;

/**
 * The result of an analysis of a product system.
 */
public class AnalysisResult {

	private FlowIndex flowIndex;
	private ProductIndex productIndex;
	private LongIndex impactCategoryIndex;

	private double[] scalingFactors;
	private IMatrix singleFlowResults;
	private IMatrix totalFlowResults;
	private IMatrix singleImpactResult;
	private IMatrix totalImpactResult;
	private IMatrix impactFactors;

	// result generators
	private AnalysisFlowResults flowResults;
	private AnalysisImpactResults impactResults;
	private AnalysisContributions contributions;
	private LinkContributions linkContributions;

	public AnalysisResult(FlowIndex flowIndex, ProductIndex productIndex) {
		this.flowIndex = flowIndex;
		this.productIndex = productIndex;
	}

	public AnalysisFlowResults getFlowResults() {
		if (flowResults == null)
			flowResults = new AnalysisFlowResults(this);
		return flowResults;
	}

	public AnalysisImpactResults getImpactResults() {
		if (impactResults == null)
			impactResults = new AnalysisImpactResults(this);
		return impactResults;
	}

	public void setLinkContributions(LinkContributions linkContributions) {
		this.linkContributions = linkContributions;
	}

	public AnalysisContributions getContributions() {
		if (contributions == null)
			contributions = new AnalysisContributions(this, linkContributions);
		return contributions;
	}

	public ProductIndex getProductIndex() {
		return productIndex;
	}

	public double[] getScalingFactors() {
		return scalingFactors;
	}

	public void setScalingFactors(double[] scalingFactors) {
		this.scalingFactors = scalingFactors;
	}

	public void setSingleResult(IMatrix singleResult) {
		this.singleFlowResults = singleResult;
	}

	public void setTotalResult(IMatrix totalResult) {
		this.totalFlowResults = totalResult;
	}

	public void setSingleImpactResult(IMatrix singleImpactResult) {
		this.singleImpactResult = singleImpactResult;
	}

	public void setTotalImpactResult(IMatrix totalImpactResult) {
		this.totalImpactResult = totalImpactResult;
	}

	public void setImpactCategoryIndex(LongIndex impactCategoryIndex) {
		this.impactCategoryIndex = impactCategoryIndex;
	}

	public LongIndex getImpactIndex() {
		return impactCategoryIndex;
	}

	public void setImpactFactors(IMatrix impactFactors) {
		this.impactFactors = impactFactors;
	}

	public double getImpactFactor(long impactCategory, long flow) {
		if (impactCategoryIndex == null)
			return 0d;
		int row = impactCategoryIndex.getIndex(impactCategory);
		int col = flowIndex.getIndex(flow);
		return getValue(impactFactors, row, col);
	}

	public FlowIndex getFlowIndex() {
		return flowIndex;
	}

	public boolean hasImpactResults() {
		return impactCategoryIndex != null && !impactCategoryIndex.isEmpty();
	}

	/**
	 * Get the scaling factor for the given process. If the process is used with
	 * several products in the product system, this returns the sum of all
	 * scaling factors for each process-product-pair.
	 */
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

	/**
	 * Get the single flow result for the given process and flow.
	 */
	public double getSingleFlowResult(long processId, long flowId) {
		int row = flowIndex.getIndex(flowId);
		double val = getValue(singleFlowResults, row, processId);
		return adoptFlowResult(val, flowId);
	}

	/**
	 * Get the total flow result for the given process-product and flow.
	 */
	public double getTotalFlowResult(LongPair processProduct, long flowId) {
		int row = flowIndex.getIndex(flowId);
		int col = productIndex.getIndex(processProduct);
		double val = getValue(totalFlowResults, row, col);
		return adoptFlowResult(val, flowId);
	}

	/**
	 * Get the upstream-total flow result for the given process and flow.
	 */
	public double getTotalFlowResult(long processId, long flowId) {
		int row = flowIndex.getIndex(flowId);
		double val = getValue(totalFlowResults, row, processId);
		return adoptFlowResult(val, flowId);
	}

	private double adoptFlowResult(double d, long flowId) {
		if (d == 0)
			return d; // avoid -0 in the results
		boolean inputFlow = flowIndex.isInput(flowId);
		return inputFlow ? -d : d;
	}

	/**
	 * Get the single impact category result for the given process and impact
	 * category.
	 */
	public double getSingleImpactResult(long processId, long impactCategory) {
		if (impactCategoryIndex == null)
			return 0;
		int row = impactCategoryIndex.getIndex(impactCategory);
		double val = getValue(singleImpactResult, row, processId);
		return val;
	}

	/**
	 * Get the upstream-total impact category result for the given
	 * process-product and impact category.
	 */
	public double getTotalImpactResult(LongPair processProduct,
			long impactCategory) {
		if (impactCategoryIndex == null)
			return 0;
		int row = impactCategoryIndex.getIndex(impactCategory);
		int col = productIndex.getIndex(processProduct);
		double val = getValue(totalImpactResult, row, col);
		return val;
	}

	/**
	 * Get the upstream-total impact category result for the given process and
	 * impact category.
	 */
	public double getTotalImpactResult(long processId, long impactCategory) {
		if (impactCategoryIndex == null)
			return 0;
		int row = impactCategoryIndex.getIndex(impactCategory);
		double val = getValue(totalImpactResult, row, processId);
		return val;
	}

	private double getValue(IMatrix matrix, int row, long processId) {
		if (matrix == null)
			return 0;
		double colSum = 0;
		for (LongPair product : productIndex.getProducts(processId)) {
			int col = productIndex.getIndex(product);
			colSum += getValue(matrix, row, col);
		}
		return colSum;
	}

	private double getValue(IMatrix matrix, int row, int col) {
		if (matrix == null)
			return 0d;
		if (row < 0 || row >= matrix.getRowDimension())
			return 0d;
		if (col < 0 || col >= matrix.getColumnDimension())
			return 0d;
		return matrix.getEntry(row, col);
	}

}
