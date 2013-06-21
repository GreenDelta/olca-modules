package org.openlca.core.model.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.openlca.core.math.FlowIndex;
import org.openlca.core.math.IMatrix;
import org.openlca.core.math.IResultData;
import org.openlca.core.math.Index;
import org.openlca.core.math.ProductIndex;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.NormalizationWeightingFactor;
import org.openlca.core.model.NormalizationWeightingSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

/**
 * The result of an analysis of a product system.
 */
public class AnalysisResult implements IResultData {

	private CalculationSetup setup;

	private FlowIndex flowIndex;
	private ProductIndex productIndex;
	private Index<ImpactCategoryDescriptor> impactCategoryIndex;

	private double[] scalingFactors;
	private IMatrix singleResult;
	private IMatrix totalResult;
	private IMatrix singleImpactResult;
	private IMatrix totalImpactResult;
	private IMatrix impactFactors;

	public AnalysisResult(CalculationSetup setup, FlowIndex flowIndex,
			ProductIndex productIndex) {
		this.setup = setup;
		this.flowIndex = flowIndex;
		this.productIndex = productIndex;
	}

	public CalculationSetup getSetup() {
		return setup;
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
		this.singleResult = singleResult;
	}

	public void setTotalResult(IMatrix totalResult) {
		this.totalResult = totalResult;
	}

	public void setSingleImpactResult(IMatrix singleImpactResult) {
		this.singleImpactResult = singleImpactResult;
	}

	public void setTotalImpactResult(IMatrix totalImpactResult) {
		this.totalImpactResult = totalImpactResult;
	}

	public void setImpactCategoryIndex(
			Index<ImpactCategoryDescriptor> impactCategoryIndex) {
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

	public List<AnalysisFlowResult> getFlowResults(Flow flow) {
		List<AnalysisFlowResult> results = new ArrayList<>();
		for (Process process : setup.getProductSystem().getProcesses()) {
			double singleResult = getSingleResult(process, flow);
			double result = getResult(process, flow);
			AnalysisFlowResult flowResult = new AnalysisFlowResult();
			flowResult.setAggregatedResult(result);
			flowResult.setProcess(process);
			flowResult.setFlow(flow);
			flowResult.setSingleResult(singleResult);
			results.add(flowResult);
		}
		return results;
	}

	/**
	 * Get the total result of the given flow (this is the result of the
	 * reference product which can be less than the maximum in the product
	 * system).
	 */
	public AnalysisFlowResult getTotalResult(Flow flow) {
		int col = productIndex.getIndex(setup.getReferenceProcess(),
				setup.getReferenceExchange());
		int row = flowIndex.getIndex(flow);
		double aggVal = adopt(totalResult.getEntry(row, col), flow);
		double singVal = adopt(singleResult.getEntry(row, col), flow);
		AnalysisFlowResult result = new AnalysisFlowResult();
		result.setAggregatedResult(aggVal);
		result.setSingleResult(singVal);
		result.setFlow(flow);
		result.setProcess(setup.getReferenceProcess());
		return result;
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
		ImpactCategoryResult totalResult = createImpactResult(category, totalVal,
				nwSet);
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
		result.setId(UUID.randomUUID().toString());
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

	public AnalysisFlowResult[][] getFlowResults(Process process) {
		List<AnalysisFlowResult> inputs = new ArrayList<>();
		List<AnalysisFlowResult> outputs = new ArrayList<>();
		for (int i = 0; i < flowIndex.size(); i++) {
			Flow flow = flowIndex.getFlowAt(i);
			double rawResult = getValue(totalResult, i, process);
			double result = adopt(rawResult, flow);
			if (result == 0)
				continue;
			double singleResult = getSingleResult(process, flow);
			AnalysisFlowResult flowResult = new AnalysisFlowResult();
			flowResult.setAggregatedResult(result);
			flowResult.setFlow(flow);
			flowResult.setProcess(process);
			flowResult.setSingleResult(singleResult);
			boolean input = isInput(flow, rawResult);
			if (input)
				inputs.add(flowResult);
			else
				outputs.add(flowResult);
		}
		return new AnalysisFlowResult[][] {
				inputs.toArray(new AnalysisFlowResult[inputs.size()]),
				outputs.toArray(new AnalysisFlowResult[outputs.size()]) };
	}

	public FlowIndex getFlowIndex() {
		return flowIndex == null ? new FlowIndex() : flowIndex;
	}

	@Override
	public Flow[] getFlows() {
		return getFlowIndex().getFlows();
	}

	@Override
	public ImpactCategoryDescriptor[] getImpactCategories() {
		if (impactCategoryIndex == null)
			return new ImpactCategoryDescriptor[0];
		return impactCategoryIndex.getItems();
	}

	@Override
	public boolean hasImpactResults() {
		return impactCategoryIndex != null && !impactCategoryIndex.isEmpty();
	}

	public int getIndex(Flow flow) {
		return flowIndex.getIndex(flow);
	}

	public double getScalingFactor(Process process) {
		double factor = 0;
		List<String> productIds = productIndex.getProducts(process);
		for (String productId : productIds) {
			int idx = productIndex.getIndex(productId);
			factor += scalingFactors[idx];
		}
		return factor;
	}

	public double getResult(Process process, Flow flow) {
		int row = flowIndex.getIndex(flow);
		double val = getValue(totalResult, row, process);
		return adopt(val, flow);
	}

	public double getResult(Process process,
			ImpactCategoryDescriptor impactCategory) {
		if (impactCategoryIndex == null)
			return 0;
		int row = impactCategoryIndex.getIndex(impactCategory);
		double val = getValue(totalImpactResult, row, process);
		return val;
	}

	public double getSingleResult(Process process, Flow flow) {
		int row = flowIndex.getIndex(flow);
		double val = getValue(singleResult, row, process);
		return adopt(val, flow);
	}

	public double getSingleResult(Process process,
			ImpactCategoryDescriptor impactCategory) {
		if (impactCategoryIndex == null)
			return 0;
		int row = impactCategoryIndex.getIndex(impactCategory);
		double val = getValue(singleImpactResult, row, process);
		return val;
	}

	private double getValue(IMatrix matrix, int row, Process process) {
		if (matrix == null || process == null)
			return 0;
		int rows = matrix.getRowDimension();
		int cols = matrix.getColumnDimension();
		if (row < 0 || row >= rows)
			return 0;
		double colSum = 0;
		for (String productId : productIndex.getProducts(process)) {
			int col = productIndex.getIndex(productId);
			if (col < 0 || col >= cols)
				continue;
			colSum += matrix.getEntry(row, col);
		}
		return colSum;
	}

	private double adopt(double d, Flow flow) {
		if (d == 0)
			return d;
		boolean inputFlow = flowIndex.isInput(flow);
		if (flow.getFlowType() == FlowType.ELEMENTARY_FLOW)
			return inputFlow ? -d : d;
		return Math.abs(d);
	}

	private boolean isInput(Flow flow, double rawValue) {
		if (flow.getFlowType() == FlowType.ELEMENTARY_FLOW
				&& flowIndex.isInput(flow))
			return true;
		return rawValue < 0;
	}

}
