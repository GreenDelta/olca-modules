package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.LcaCalculator;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.solutions.ResultProvider;

/**
 * The `FullResult` extends the `ContributionResult`. It contains additionally
 * the upstream contributions to LCI, LCIA, and LCC results where applicable.
 */
public class FullResult extends ContributionResult {

	public static FullResult of(MatrixData matrices) {
		var calculator = new LcaCalculator(matrices);
		return calculator.calculateFull();
	}

	public static FullResult of(IDatabase db, ProductSystem system) {
		var setup = new CalculationSetup(system);
		return of (db, setup);
	}

	public static FullResult of(IDatabase db, CalculationSetup setup) {
		var calculator = new SystemCalculator(db);
		if (db.hasLibraries()) {
			calculator.withLibraries(LibraryDir.getDefault());
		}
		return calculator.calculateFull(setup);
	}

	public FullResult(ResultProvider solution) {
		super(solution);
	}

	/**
	 * Get the upstream contribution of the given process-product pair $j$ to the
	 * inventory result of elementary flow $i$: $\mathbf{U}[i,j]$.
	 */
	public double getUpstreamFlowResult(ProcessProduct product, IndexFlow flow) {
		if (!hasFlowResults())
			return 0;
		int flowIdx = flowIndex.of(flow);
		int productIdx = techIndex.getIndex(product);
		if (flowIdx < 0 || productIdx < 0)
			return 0;
		double amount = provider.totalFlowOf(flowIdx, productIdx);
		return adopt(flow, amount);
	}

	/**
	 * Get the upstream contribution of the given process $j$ to the inventory
	 * result of elementary flow $i$. When the process has multiple products it is
	 * the sum of the contributions of all of these process-product pairs.
	 */
	public double getUpstreamFlowResult(
			CategorizedDescriptor process, IndexFlow flow) {
		double total = 0;
		for (var p : techIndex.getProviders(process)) {
			total += getUpstreamFlowResult(p, flow);
		}
		return total;
	}

	/**
	 * Get the upstream contributions of the given process $j$ to the inventory
	 * result of all elementary flows in the product system.
	 */
	public List<FlowResult> getUpstreamFlowResults(
			CategorizedDescriptor process) {
		var results = new ArrayList<FlowResult>();
		flowIndex.each((i, flow) -> {
			double value = getUpstreamFlowResult(process, flow);
			results.add(new FlowResult(flow, value));
		});
		return results;
	}

	/**
	 * Get the upstream contribution of the given process-product pair $j$ to the
	 * LCIA category result $j$: $\mathbf{V}[i,j]$.
	 */
	public double getUpstreamImpactResult(
			ProcessProduct product, ImpactDescriptor impact) {
		if (!hasImpactResults())
			return 0;
		int impactIdx = impactIndex.of(impact);
		int productIdx = techIndex.getIndex(product);
		return impactIdx < 0 || productIdx < 0
				? 0
				: provider.totalImpactOf(impactIdx, productIdx);
	}

	/**
	 * Get the upstream contribution of the given process $j$ to the LCIA category
	 * result $i$. When the process has multiple products it is the sum of the
	 * contributions of all of these process-product pairs.
	 */
	public double getUpstreamImpactResult(
			CategorizedDescriptor process, ImpactDescriptor impact) {
		double total = 0;
		for (var p : techIndex.getProviders(process)) {
			total += getUpstreamImpactResult(p, impact);
		}
		return total;
	}

	/**
	 * Get the upstream contributions of the given process $j$ to the LCIA category
	 * results.
	 */
	public List<ImpactResult> getUpstreamImpactResults(
			CategorizedDescriptor process) {
		var results = new ArrayList<ImpactResult>();
		if (!hasImpactResults())
			return results;
		impactIndex.each((i, impact) -> {
			var r = new ImpactResult();
			r.impact = impact;
			r.value = getUpstreamImpactResult(process, impact);
			results.add(r);
		});
		return results;
	}

	/**
	 * Get the upstream contribution of the given process-product pair $j$ to the
	 * LCC result: $\mathbf{k}_u[j]$.
	 */
	public double getUpstreamCostResult(ProcessProduct product) {
		if (!hasCostResults())
			return 0;
		int productIdx = techIndex.getIndex(product);
		return productIdx < 0
				? 0
				: provider.totalCostsOf(productIdx);
	}

	/**
	 * Get the upstream contribution of the given process $j$ to the LCC result.
	 * When the process has multiple products it is the sum of the contributions of
	 * all of these process-product pairs.
	 */
	public double getUpstreamCostResult(CategorizedDescriptor process) {
		double total = 0;
		for (var p : techIndex.getProviders(process)) {
			total += getUpstreamCostResult(p);
		}
		return total;
	}

	/**
	 * Get the contribution share of the outgoing process product (provider) to the
	 * product input (recipient) of the given link and the calculated product
	 * system. The returned share is a value between 0 and 1.
	 */
	public double getLinkShare(ProcessLink link) {

		var provider = techIndex.getProvider(link.providerId, link.flowId);
		int providerIdx = techIndex.getIndex(provider);
		if (providerIdx < 0)
			return 0;

		double amount = 0.0;
		for (var process : techIndex.getProviders(link.processId)) {
			int processIdx = techIndex.getIndex(process);
			amount += this.provider.scaledTechValueOf(providerIdx, processIdx);
		}
		if (amount == 0)
			return 0;

		double total = this.provider.scaledTechValueOf(providerIdx, providerIdx);
		return total == 0
				? 0
				: -amount / total;
	}

	/**
	 * Calculate the upstream tree for the given flow.
	 */
	public UpstreamTree getTree(IndexFlow flow) {
		int i = flowIndex.of(flow);
		double total = getTotalFlowResult(flow);
		return new UpstreamTree(flow, this, total,
				product -> provider.totalFlowOfOne(i, product));
	}

	/**
	 * Calculate the upstream tree for the given LCIA category.
	 */
	public UpstreamTree getTree(ImpactDescriptor impact) {
		int i = impactIndex.of(impact.id);
		double total = getTotalImpactResult(impact);
		return new UpstreamTree(impact, this, total,
				product -> provider.totalImpactOfOne(i, product));
	}

	/**
	 * Calculate the upstream tree for the LCC result as costs.
	 */
	public UpstreamTree getCostTree() {
		return new UpstreamTree(this, totalCosts,
				provider::totalCostsOfOne);
	}

	/**
	 * Calculate the upstream tree for the LCC result as added value.
	 */
	public UpstreamTree getAddedValueTree() {
		return new UpstreamTree(this, -totalCosts,
				product -> -provider.totalCostsOfOne(product));
	}
}
