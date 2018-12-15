package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.matrix.DIndex;
import org.openlca.core.matrix.Provider;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.CategoryDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

/**
 * A contribution result extends a simple result and contains all single
 * contributions of processes and products to the overall inventory and impact
 * assessment results. Additionally, it contains the contributions of the single
 * inventory flows to impact category results.
 */
public class ContributionResult extends SimpleResult {

	/**
	 * This is a matrix with single flow results where the flows are mapped to
	 * the rows and the process-products to the columns. Inputs have negative
	 * values here.
	 */
	public IMatrix singleFlowResults;

	/**
	 * The single LCIA category results in a matrix where the LCIA categories
	 * are mapped to the rows and the process-products to the columns.
	 */
	public IMatrix singleImpactResults;

	/**
	 * Contains the direct net-costs. Each entry contains the net-costs of the
	 * respective process-product at the respective index.
	 */
	public double[] singleCostResults;

	/**
	 * The single LCIA category result for each intervention flow in a matrix
	 * where the LCIA categories are mapped to the rows and the intervention
	 * flows to the columns.
	 */
	public IMatrix singleFlowImpacts;

	/**
	 * Contains the characterization factors in a matrix where the LCIA
	 * categories are mapped to rows and elementary flows to columns.
	 */
	public IMatrix impactFactors;

	/**
	 * Get the single flow result of the flow with the given ID for the given
	 * process-product. Inputs have negative values here.
	 *
	 * TODO: doc
	 */
	public double getDirectFlowResult(Provider provider, FlowDescriptor flow) {
		if (!hasFlowResults())
			return 0;
		int row = flowIndex.of(flow);
		int col = techIndex.getIndex(provider);
		return adopt(flow, getValue(singleFlowResults, row, col));
	}

	public double getDirectFlowResult(CategorizedDescriptor d,
			FlowDescriptor flow) {
		double total = 0;
		for (Provider p : techIndex.getProviders(d)) {
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
				getProviderHosts(),
				getTotalFlowResult(flow),
				d -> getDirectFlowResult(d, flow));
	}

	/**
	 * Get the single LCIA category result of the LCIA category with the given
	 * ID for the given process-product.
	 *
	 * TODO: doc
	 */
	public double getDirectImpactResult(Provider provider,
			ImpactCategoryDescriptor impact) {
		if (!hasImpactResults())
			return 0;
		int row = impactIndex.of(impact);
		int col = techIndex.getIndex(provider);
		return getValue(singleImpactResults, row, col);
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
		for (Provider p : techIndex.getProviders(d)) {
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
				getProviderHosts(),
				getTotalImpactResult(impact),
				d -> getDirectImpactResult(d, impact));
	}

	public double getDirectCostResult(Provider provider) {
		if (!hasCostResults())
			return 0;
		int col = techIndex.getIndex(provider);
		if (col >= singleCostResults.length)
			return 0;
		return singleCostResults[col];
	}

	public double getDirectCostResult(CategorizedDescriptor d) {
		double total = 0;
		for (Provider provider : techIndex.getProviders(d)) {
			total += getDirectCostResult(provider);
		}
		return total;
	}

	public ContributionSet<CategorizedDescriptor> getProcessCostContributions() {
		return Contributions.calculate(
				getProviderHosts(),
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
		return getValue(singleFlowImpacts, row, col);
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
		for (Provider provider : techIndex.getProviders(providerID)) {
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
