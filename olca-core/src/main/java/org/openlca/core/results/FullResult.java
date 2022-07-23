package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.MatrixIndex;
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
 * The `ContributionResult` extends the `SimpleResult` type. It also contains
 * all direct contributions of the processes to the LCI and LCIA results.
 * Additionally, it contains the contributions of the (elementary) flows to the
 * LCIA results.
 */
public class FullResult extends SimpleResult {

	public FullResult(ResultProvider provider) {
		super(provider);
	}

	public static FullResult of(IDatabase db, MatrixData data) {
		return of(SolverContext.of(db, data));
	}

	public static FullResult of(SolverContext context) {
		var provider = ResultProviders.solveLazy(context);
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

	public double getTotalRequirementsOf(TechFlow techFlow) {
		int idx = techIndex().of(techFlow);
		return idx >= 0
			? provider.totalRequirementsOf(idx)
			: 0;
	}

	/**
	 * Get the direct contribution of the given process-product pair $j$ to the
	 * inventory result of elementary flow $i$: $\mathbf{G}[i,j]$.
	 */
	public double getDirectFlowResult(TechFlow product, EnviFlow flow) {
		if (!hasEnviFlows())
			return 0;
		int flowIdx = enviIndex().of(flow);
		int productIdx = techIndex().of(product);
		if (flowIdx < 0 || productIdx < 0)
			return 0;
		double value = provider.directFlowOf(flowIdx, productIdx);
		return adopt(flow, value);
	}

	/**
	 * Get the direct contribution of the given process $j$ to the inventory result
	 * of elementary flow $i$. When the process has multiple products it is the sum
	 * of the contributions of all of these process-product pairs.
	 */
	public double getDirectFlowResult(
		RootDescriptor process, EnviFlow flow) {
		double total = 0;
		for (var p : techIndex().getProviders(process)) {
			total += getDirectFlowResult(p, flow);
		}
		return total;
	}

	/**
	 * Get the direct contributions of the given process $j$ to the inventory result
	 * of all elementary flows in the product system.
	 */
	public List<FlowValue> getFlowContributions(
		RootDescriptor process) {
		if (!hasEnviFlows())
			return Collections.emptyList();
		var results = new ArrayList<FlowValue>();
		enviIndex().each((i, flow) -> {
			double value = getDirectFlowResult(process, flow);
			results.add(new FlowValue(flow, value));
		});
		return results;
	}

	/**
	 * Get the direct contributions of the processes in the system to the inventory
	 * result of the given flow.
	 */
	public List<Contribution<TechFlow>> getProcessContributions(EnviFlow flow) {
		return Contributions.calculate(
			techIndex(),
			getTotalFlowResult(flow),
			d -> getDirectFlowResult(d, flow));
	}

	/**
	 * Get the direct contribution of the given process-product pair $j$ to the LCIA
	 * category result $j$: $\mathbf{D}[i,j]$.
	 */
	public double getDirectImpactResult(
		TechFlow product, ImpactDescriptor impact) {
		if (!hasImpacts())
			return 0;
		int impactIdx = impactIndex().of(impact);
		int productIdx = techIndex().of(product);
		return impactIdx < 0 || productIdx < 0
			? 0
			: provider.directImpactOf(impactIdx, productIdx);
	}

	/**
	 * Get the direct contribution of the given process $j$ to the LCIA category
	 * result $i$. When the process has multiple products it is the sum of the
	 * contributions of all of these process-product pairs.
	 */
	public double getDirectImpactResult(
		RootDescriptor process, ImpactDescriptor impact) {
		double total = 0;
		for (var p : techIndex().getProviders(process)) {
			total += getDirectImpactResult(p, impact);
		}
		return total;
	}

	/**
	 * Get the direct contributions of the given process to the LCIA category
	 * results.
	 */
	public List<ImpactValue> getImpactContributions(RootDescriptor process) {
		var impactIdx = impactIndex();
		if (MatrixIndex.isAbsent(impactIdx))
			return Collections.emptyList();
		var results = new ArrayList<ImpactValue>();
		for (var impact : impactIdx) {
			var amount = getDirectImpactResult(process, impact);
			results.add(new ImpactValue(impact, amount));
		}
		return results;
	}

	/**
	 * Get the direct contributions of the processes in the system to the LCIA
	 * result of the given LCIA category.
	 */
	public List<Contribution<TechFlow>> getProcessContributions(
		ImpactDescriptor impact) {
		return Contributions.calculate(
			techIndex(),
			getTotalImpactResult(impact),
			d -> getDirectImpactResult(d, impact));
	}

	/**
	 * Get the direct contribution of the given process-product pair $j$ to the LCC
	 * result: $\mathbf{k}_s[j]$.
	 */
	public double getDirectCostResult(TechFlow product) {
		int col = techIndex().of(product);
		return col < 0
			? 0
			: provider.directCostsOf(col);
	}

	/**
	 * Get the direct contribution of the given process $j$ to the LCC result. When
	 * the process has multiple products it is the sum of the contributions of all
	 * of these process-product pairs.
	 */
	public double getDirectCostResult(RootDescriptor process) {
		double total = 0;
		for (var provider : techIndex().getProviders(process)) {
			total += getDirectCostResult(provider);
		}
		return total;
	}

	/**
	 * Get the direct contributions of all processes to the LCC result.
	 */
	public List<Contribution<TechFlow>> getProcessCostContributions() {
		return Contributions.calculate(
			techIndex(),
			totalCosts,
			this::getDirectCostResult);
	}

	/**
	 * Get the direct contribution of the given elementary flow to the LCIA result
	 * of the given LCIA category.
	 */
	public double getDirectFlowImpact(EnviFlow flow, ImpactDescriptor impact) {
		if (!hasImpacts())
			return 0;
		int impactIdx = impactIndex().of(impact);
		int flowIdx = enviIndex().of(flow);
		return impactIdx < 0 || flowIdx < 0
			? 0
			: provider.flowImpactOf(impactIdx, flowIdx);
	}

	/**
	 * Get the contributions of all elementary flows to the given LCA category.
	 */
	public List<FlowValue> getFlowContributions(
		ImpactDescriptor impact) {
		var results = new ArrayList<FlowValue>();
		enviIndex().each((i, flow) -> {
			double value = getDirectFlowImpact(flow, impact);
			results.add(new FlowValue(flow, value));
		});
		return results;
	}

	/**
	 * Get the characterization factor for the given flow (and location in case of a
	 * regionalized result).
	 */
	public double getImpactFactor(
		ImpactDescriptor impact, EnviFlow flow) {
		if (!hasImpacts())
			return 0;
		int impactIdx = impactIndex().of(impact);
		int flowIdx = enviIndex().of(flow);
		if (impactIdx < 0 || flowIdx < 0)
			return 0;

		double value = provider.impactFactorOf(impactIdx, flowIdx);
		if (!flow.isInput())
			return value;

		// characterization factors for input flows are negative in the
		// matrix. A simple abs() is not correct because the original
		// characterization factor maybe was already negative (-(-(f))).
		return value == 0
			? 0 // avoid -0
			: -value;
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
