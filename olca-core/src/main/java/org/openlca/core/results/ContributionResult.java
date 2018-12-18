package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.CategoryDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

/**
 * The `ContributionResult` extends the `SimpleResult` type by also containing
 * all direct contributions of the processes to the LCI and LCIA results.
 * Additionally, it contains the contributions of the (elementary) flows to the
 * LCIA results.
 */
public class ContributionResult extends SimpleResult {

	/**
	 * A (elementary) flow * process matrix which contains the direct
	 * (elementary) flow results. This is the scaled intervention matrix:
	 * 
	 * $$\mathbf{B} \ \text{diag}(\mathbf{s})$$
	 */
	public IMatrix directFlowResults;

	/**
	 * The single LCIA category results in a matrix where the LCIA categories
	 * are mapped to the rows and the process-products to the columns.
	 */
	public IMatrix directImpactResults;

	/**
	 * Contains the direct net-costs. Each entry contains the net-costs of the
	 * respective process-product at the respective index.
	 */
	public double[] directCostResults;

	/**
	 * The single LCIA category result for each intervention flow in a matrix
	 * where the LCIA categories are mapped to the rows and the intervention
	 * flows to the columns.
	 */
	public IMatrix directFlowImpacts;

	/**
	 * Contains the characterization factors in a matrix where the LCIA
	 * categories are mapped to rows and elementary flows to columns.
	 */
	public IMatrix impactFactors;

	@Override
	public boolean hasCostResults() {
		return directCostResults != null;
	}

	/**
	 * Get the single flow result of the flow with the given ID for the given
	 * process-product. Inputs have negative values here.
	 *
	 * TODO: doc
	 */
	public double getDirectFlowResult(ProcessProduct provider, FlowDescriptor flow) {
		if (!hasFlowResults())
			return 0;
		int row = flowIndex.of(flow);
		int col = techIndex.getIndex(provider);
		return adopt(flow, getValue(directFlowResults, row, col));
	}

	public double getDirectFlowResult(CategorizedDescriptor d,
			FlowDescriptor flow) {
		double total = 0;
		for (ProcessProduct p : techIndex.getProviders(d)) {
			total += getDirectFlowResult(p, flow);
		}
		return total;
	}

	public List<FlowResult> getFlowContributions(CategorizedDescriptor d) {
		List<FlowResult> results = new ArrayList<>();
		flowIndex.each(flow -> {
			FlowResult r = new FlowResult();
			r.flow = flow;
			r.input = flowIndex.isInput(flow.getId());
			r.value = getDirectFlowResult(d, flow);
			results.add(r);
		});
		return results;
	}

	/**
	 * Get the single contributions of the processes to the total result of the
	 * given flow.
	 *
	 * TODO: doc
	 */
	public ContributionSet<CategorizedDescriptor> getProcessContributions(
			FlowDescriptor flow) {
		return Contributions.calculate(
				getProcesses(),
				getTotalFlowResult(flow),
				d -> getDirectFlowResult(d, flow));
	}

	/**
	 * Get the single LCIA category result of the LCIA category with the given
	 * ID for the given process-product.
	 *
	 * TODO: doc
	 */
	public double getDirectImpactResult(ProcessProduct provider,
			ImpactCategoryDescriptor impact) {
		if (!hasImpactResults())
			return 0;
		int row = impactIndex.of(impact);
		int col = techIndex.getIndex(provider);
		return getValue(directImpactResults, row, col);
	}

	/**
	 * Get the sum of the single LCIA category results of the LCIA category with
	 * the given ID for all products of the process with the given ID.
	 *
	 * TODO: doc
	 */
	public double getDirectImpactResult(CategorizedDescriptor d,
			ImpactCategoryDescriptor impact) {
		double total = 0;
		for (ProcessProduct p : techIndex.getProviders(d)) {
			total += getDirectImpactResult(p, impact);
		}
		return total;
	}

	public List<ImpactResult> getImpactContributions(
			CategoryDescriptor d) {
		List<ImpactResult> results = new ArrayList<>();
		impactIndex.each(impact -> {
			ImpactResult r = new ImpactResult();
			r.impactCategory = impact;
			r.value = getDirectImpactResult(d, impact);
			results.add(r);
		});
		return results;
	}

	public ContributionSet<CategorizedDescriptor> getProcessContributions(
			ImpactCategoryDescriptor impact) {
		return Contributions.calculate(
				getProcesses(),
				getTotalImpactResult(impact),
				d -> getDirectImpactResult(d, impact));
	}

	public double getDirectCostResult(ProcessProduct provider) {
		if (!hasCostResults())
			return 0;
		int col = techIndex.getIndex(provider);
		if (col >= directCostResults.length)
			return 0;
		return directCostResults[col];
	}

	public double getDirectCostResult(CategorizedDescriptor d) {
		double total = 0;
		for (ProcessProduct provider : techIndex.getProviders(d)) {
			total += getDirectCostResult(provider);
		}
		return total;
	}

	public ContributionSet<CategorizedDescriptor> getProcessCostContributions() {
		return Contributions.calculate(
				getProcesses(),
				totalCosts,
				d -> getDirectCostResult(d));
	}

	/**
	 * Get the single LCIA category result of the LCIA category with the given
	 * ID for the given process-product.
	 *
	 * TODO: doc
	 */
	public double getDirectFlowImpact(FlowDescriptor flow,
			ImpactCategoryDescriptor impact) {
		if (!hasImpactResults())
			return 0;
		int row = impactIndex.of(impact);
		int col = flowIndex.of(flow);
		return getValue(directFlowImpacts, row, col);
	}

	public List<FlowResult> getFlowContributions(
			ImpactCategoryDescriptor impact) {
		List<FlowResult> results = new ArrayList<>();
		flowIndex.each(flow -> {
			FlowResult r = new FlowResult();
			r.flow = flow;
			r.input = flowIndex.isInput(flow);
			r.value = getDirectFlowImpact(flow, impact);
			results.add(r);
		});
		return results;
	}

	@Deprecated
	protected double getProviderValue(IMatrix matrix, int row,
			long providerID) {
		if (matrix == null)
			return 0;
		double colSum = 0;
		for (ProcessProduct provider : techIndex.getProviders(providerID)) {
			int col = techIndex.getIndex(provider);
			colSum += getValue(matrix, row, col);
		}
		return colSum;
	}

	protected double getValue(IMatrix matrix, int row, int col) {
		if (matrix == null)
			return 0d;
		if (row < 0 || row >= matrix.rows())
			return 0d;
		if (col < 0 || col >= matrix.columns())
			return 0d;
		return matrix.get(row, col);
	}

}
