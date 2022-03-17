package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.providers.ResultProvider;
import org.openlca.core.results.providers.ResultProviders;
import org.openlca.core.results.providers.SolverContext;

/**
 * The `FullResult` extends the `ContributionResult`. It contains additionally
 * the upstream contributions to LCI, LCIA, and LCC results where applicable.
 */
public class FullResult extends ContributionResult {

	public static FullResult of(IDatabase db, MatrixData data) {
		return of(SolverContext.of(db, data));
	}

	public static FullResult of(SolverContext context) {
		var provider = ResultProviders.eagerOf(context);
		return new FullResult(provider);
	}

	public static FullResult of(IDatabase db, ProductSystem system) {
		var setup = CalculationSetup.fullAnalysis(system);
		return of(db, setup);
	}

	public static FullResult of(IDatabase db, CalculationSetup setup) {
		var calculator = new SystemCalculator(db);
		return calculator.calculateFull(setup);
	}

	public FullResult(ResultProvider solution) {
		super(solution);
	}

	/**
	 * Get the upstream contribution of the given process-product pair $j$ to the
	 * inventory result of elementary flow $i$: $\mathbf{U}[i,j]$.
	 */
	public double getUpstreamFlowResult(TechFlow product, EnviFlow flow) {
		var flowIndex = enviIndex();
		if (flowIndex == null)
			return 0;
		int flowIdx = flowIndex.of(flow);
		int productIdx = techIndex().of(product);
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
            RootDescriptor process, EnviFlow flow) {
		double total = 0;
		for (var p : techIndex().getProviders(process)) {
			total += getUpstreamFlowResult(p, flow);
		}
		return total;
	}

	/**
	 * Get the upstream contributions of the given process $j$ to the inventory
	 * result of all elementary flows in the product system.
	 */
	public List<FlowValue> getUpstreamFlowResults(
		RootDescriptor process) {
		var flowIndex = enviIndex();
		if (flowIndex == null)
			return Collections.emptyList();
		var results = new ArrayList<FlowValue>();
		flowIndex.each((i, flow) -> {
			double value = getUpstreamFlowResult(process, flow);
			results.add(new FlowValue(flow, value));
		});
		return results;
	}

	/**
	 * Get the upstream contribution of the given process-product pair $j$ to the
	 * LCIA category result $j$: $\mathbf{V}[i,j]$.
	 */
	public double getUpstreamImpactResult(
		TechFlow product, ImpactDescriptor impact) {
		if (!hasImpacts())
			return 0;
		int impactIdx = impactIndex().of(impact);
		int productIdx = techIndex().of(product);
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
            RootDescriptor process, ImpactDescriptor impact) {
		double total = 0;
		for (var p : techIndex().getProviders(process)) {
			total += getUpstreamImpactResult(p, impact);
		}
		return total;
	}

	/**
	 * Get the upstream contributions of the given process $j$ to the LCIA category
	 * results.
	 */
	public List<ImpactValue> getUpstreamImpactResults(
		RootDescriptor process) {
		var results = new ArrayList<ImpactValue>();
		if (!hasImpacts())
			return results;
		impactIndex().each((i, impact) -> {
			double amount = getUpstreamImpactResult(process, impact);
			results.add(new ImpactValue(impact, amount));
		});
		return results;
	}

	/**
	 * Get the upstream contribution of the given process-product pair $j$ to the
	 * LCC result: $\mathbf{k}_u[j]$.
	 */
	public double getUpstreamCostResult(TechFlow product) {
		if (!this.hasCosts())
			return 0;
		int productIdx = techIndex().of(product);
		return productIdx < 0
			? 0
			: provider.totalCostsOf(productIdx);
	}

	/**
	 * Get the upstream contribution of the given process $j$ to the LCC result.
	 * When the process has multiple products it is the sum of the contributions of
	 * all of these process-product pairs.
	 */
	public double getUpstreamCostResult(RootDescriptor process) {
		double total = 0;
		for (var p : techIndex().getProviders(process)) {
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

		var techIndex = techIndex();
		var provider = techIndex.getProvider(link.providerId, link.flowId);
		int providerIdx = techIndex.of(provider);
		if (providerIdx < 0)
			return 0;

		double amount = 0.0;
		for (var process : techIndex.getProviders(link.processId)) {
			int processIdx = techIndex.of(process);
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
	public UpstreamTree getTree(EnviFlow flow) {
		int i = enviIndex().of(flow);
		double total = getTotalFlowResult(flow);
		return new UpstreamTree(flow, this, total,
			product -> provider.totalFlowOfOne(i, product));
	}

	/**
	 * Calculate the upstream tree for the given LCIA category.
	 */
	public UpstreamTree getTree(ImpactDescriptor impact) {
		int i = impactIndex().of(impact.id);
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
