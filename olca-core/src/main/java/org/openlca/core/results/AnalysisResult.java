package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.indices.FlowIndex;
import org.openlca.core.indices.LongIndex;
import org.openlca.core.indices.LongPair;
import org.openlca.core.indices.ProductIndex;
import org.openlca.core.math.IMatrix;
import org.openlca.core.math.IResultData;
import org.openlca.core.model.Flow;
import org.openlca.core.model.NormalizationWeightingFactor;
import org.openlca.core.model.NormalizationWeightingSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

/**
 * The result of an analysis of a product system.
 */
public class AnalysisResult implements IResultData {

	private FlowIndex flowIndex;
	private ProductIndex productIndex;
	private LongIndex impactCategoryIndex;

	private double[] scalingFactors;
	private IMatrix singleFlowResults;
	private IMatrix totalFlowResults;
	private IMatrix singleImpactResult;
	private IMatrix totalImpactResult;
	private IMatrix impactFactors;

	public AnalysisResult(FlowIndex flowIndex, ProductIndex productIndex) {
		this.flowIndex = flowIndex;
		this.productIndex = productIndex;
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

	public void setImpactFactors(IMatrix impactFactors) {
		this.impactFactors = impactFactors;
	}

	public double getImpactFactor(ImpactCategoryDescriptor impact, Flow flow) {
		if (impactCategoryIndex == null || flowIndex == null
				|| impactFactors == null)
			return 0d;
		int row = impactCategoryIndex.getIndex(impact);
		int col = flowIndex.getIndex(flow);
		if (row < 0 || col < 0 || row >= impactFactors.getRowDimension()
				|| col >= impactFactors.getColumnDimension())
			return 0d;
		return impactFactors.getEntry(row, col);
	}

	/** Get the impact assessment result for the given process and category. */
	public AnalysisImpactResult getImpactResult(Process process,
			ImpactCategoryDescriptor category, NormalizationWeightingSet nwSet) {
		return createImpactResult(process, category, nwSet);
	}

	/**
	 * Get the impact assessment results of the given impact category for each
	 * process.
	 */
	public List<AnalysisImpactResult> getImpactResults(
			ImpactCategoryDescriptor category) {
		List<AnalysisImpactResult> list = new ArrayList<>();
		for (Process process : setup.getProductSystem().getProcesses()) {
			AnalysisImpactResult r = getImpactResult(process, category, null);
			list.add(r);
		}
		return list;
	}

	/**
	 * Get the impact assessment results of the given categories for all
	 * processes in the product system.
	 */
	public List<AnalysisImpactResult> getImpactResults(
			ImpactCategoryDescriptor category, NormalizationWeightingSet nwSet) {
		if (category == null || impactCategoryIndex == null)
			return Collections.emptyList();
		List<AnalysisImpactResult> results = new ArrayList<>();
		for (Process process : setup.getProductSystem().getProcesses()) {
			AnalysisImpactResult result = createImpactResult(process, category,
					nwSet);
			results.add(result);
		}
		return results;
	}

	/**
	 * Get the impact assessment results for all impact categories of the given
	 * process.
	 */
	public List<AnalysisImpactResult> getImpactResults(Process process,
			NormalizationWeightingSet nwSet) {
		if (impactCategoryIndex == null || totalImpactResult == null)
			return Collections.emptyList();
		List<AnalysisImpactResult> input = new ArrayList<>();
		for (ImpactCategoryDescriptor descriptor : impactCategoryIndex
				.getItems()) {
			AnalysisImpactResult res = createImpactResult(process, descriptor,
					nwSet);
			if (res != null)
				input.add(res);
		}
		return input;
	}

	private AnalysisImpactResult createImpactResult(Process process,
			ImpactCategoryDescriptor category, NormalizationWeightingSet nwSet) {
		int row = impactCategoryIndex.getIndex(category);
		double singleVal = getValue(singleImpactResult, row, process);
		double totalVal = getValue(totalImpactResult, row, process);
		ImpactCategoryResult singleResult = createImpactResult(category,
				singleVal, nwSet);
		ImpactCategoryResult totalResult = createImpactResult(category,
				totalVal, nwSet);
		AnalysisImpactResult result = new AnalysisImpactResult();
		result.setAggregatedResult(totalResult);
		result.setCategory(category.getName());
		result.setProcess(process);
		result.setSingleResult(singleResult);
		return result;
	}

	private ImpactCategoryResult createImpactResult(
			ImpactCategoryDescriptor descriptor, double val,
			NormalizationWeightingSet nwSet) {
		ImpactCategoryResult result = new ImpactCategoryResult();
		result.setCategory(descriptor.getName());
		result.setUnit(descriptor.getReferenceUnit());
		result.setValue(val);
		if (nwSet == null)
			return result;
		NormalizationWeightingFactor fac = nwSet.getFactor(descriptor);
		if (fac == null)
			return result;
		if (fac.getNormalizationFactor() != null)
			result.setNormalizationFactor(fac.getNormalizationFactor());
		if (fac.getWeightingFactor() != null) {
			result.setWeightingFactor(fac.getWeightingFactor());
			result.setWeightingUnit(nwSet.getUnit());
		}
		return result;
	}

	public FlowIndex getFlowIndex() {
		return flowIndex;
	}

	@Override
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
	 * Get the upstream-total flow result for the given process and flow.
	 */
	public double getTotalFlowResult(long processId, long flowId) {
		int row = flowIndex.getIndex(flowId);
		double val = getValue(totalFlowResults, row, processId);
		return adoptFlowResult(val, flowId);
	}

	private double adoptFlowResult(double d, long flowId) {
		if (d == 0)
			return d;
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
