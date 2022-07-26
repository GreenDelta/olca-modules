package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.MatrixIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.providers.ResultProvider;
import org.openlca.core.results.providers.ResultProviders;
import org.openlca.core.results.providers.SolverContext;

public class LcaResult implements IResult {

	private final ResultProvider provider;
	private final Map<TechFlow, LcaResult> subResults;

	public LcaResult(ResultProvider provider) {
		this.provider = Objects.requireNonNull(provider);
		this.subResults = new HashMap<>();
	}

	public static LcaResult of(IDatabase db, MatrixData data) {
		return of(SolverContext.of(db, data));
	}

	public static LcaResult of(SolverContext context) {
		var provider = ResultProviders.solve(context);
		return new LcaResult(provider);
	}

	public static LcaResult of(IDatabase db, ProductSystem system) {
		var setup = CalculationSetup.fullAnalysis(system);
		return of(db, setup);
	}

	public static LcaResult of(IDatabase db, CalculationSetup setup) {
		var calculator = new SystemCalculator(db);
		return calculator.calculate(setup);
	}

	/**
	 * Returns the underlying result provider of this result.
	 */
	public ResultProvider provider() {
		return provider;
	}

	@Override
	public Demand demand() {
		return provider.demand();
	}

	@Override
	public TechIndex techIndex() {
		return provider.techIndex();
	}

	@Override
	public EnviIndex enviIndex() {
		return provider.enviIndex();
	}

	@Override
	public ImpactIndex impactIndex() {
		return provider.impactIndex();
	}

	@Override
	public boolean hasEnviFlows() {
		return provider.hasFlows();
	}

	@Override
	public boolean hasImpacts() {
		return provider.hasImpacts();
	}

	@Override
	public boolean hasCosts() {
		return provider.hasCosts();
	}

	/**
	 * Returns the scaling vector of the result.
	 *
	 * The scaling vector {@code s} is calculated by solving the equation {@code A
	 * * s = f} where {@code A} is the technology matrix and {@code f} the final
	 * demand vector of the product system.
	 */
	public double[] scalingVector() {
		return provider().scalingVector();
	}

	/**
	 * Returns the total requirements vector.
	 *
	 * The total requirements are the respective product amounts fulfill the
	 * demand of the product system. As our technology matrix {@code A} is indexed
	 * symmetrically (means rows and columns refer to the same process-product
	 * pair) our product amounts are on the diagonal of the technology matrix
	 * {@code A} and the total requirements can be calculated by the following
	 * equation where {@code s} is the scaling vector ({@code °} denotes
	 * element-wise multiplication): {@code t = diag(A) ° s}
	 */
	public double[] totalRequirements() {
		return provider.totalRequirements();
	}

	/**
	 * Returns the total flow / inventory result of a product system.
	 *
	 * The total inventory result {@code g} can be calculated via {@code g = B *
	 * s} where {@code B} is the intervention matrix and {@code s} the scaling
	 * vector. Note that inputs have negative values in this vector.
	 */
	public double[] totalFlowResults() {
		return provider.totalFlows();
	}

	/**
	 * Returns the total impact assessment result vector of a product system.
	 *
	 * The total impact assessment result {@code h} can be calculated via: {@code
	 * h = C * g} where {@code C} is a {@code flow * impact category} matrix that
	 * contains the characterization factors and {@code g} the total inventory
	 * result of the system.
	 */
	public double[] totalImpactResults() {
		return provider.totalImpacts();
	}

	/**
	 * Returns the total net-life-cycle-costs of a product system.
	 *
	 * These costs {@code k_t} can be calculated via {@code k_t = k ° s} where
	 * {@code k_j} are the net-costs of process {@code j} and {@code s_j} is the
	 * scaling factor of that process.
	 */
	public double totalCosts() {
		return provider.totalCosts();
	}

	/**
	 * Get the scaling factor $\mathbf{s}_j$ of the given process-product pair
	 * $j$.
	 */
	public double getScalingFactor(TechFlow product) {
		int idx = techIndex().of(product);
		var scalingVector = scalingVector();
		if (idx < 0 || idx > scalingVector.length)
			return 0;
		return scalingVector[idx];
	}

	/**
	 * Get the scaling factor $\mathbf{s}_j$ of the given process $j$. When the
	 * process has multiple products in the system it returns the sum of the
	 * scaling factors of all of these process-product pairs.
	 */
	public double getScalingFactor(RootDescriptor process) {
		double factor = 0;
		for (TechFlow p : techIndex().getProviders(process)) {
			factor += getScalingFactor(p);
		}
		return factor;
	}

	/**
	 * Get the total inventory result $\mathbf{g}_i$ of the given flow $i$.
	 */
	public double getTotalFlowResult(EnviFlow flow) {
		var flowIndex = enviIndex();
		if (flowIndex == null)
			return 0;
		int idx = flowIndex.of(flow);
		var totalFlows = totalFlowResults();
		if (idx < 0 || idx >= totalFlows.length)
			return 0;
		return adopt(flow, totalFlows[idx]);
	}

	/**
	 * Returns the flow results of the inventory result $\mathbf{g}$.
	 */
	public List<FlowValue> getTotalFlowResults() {
		var flowIndex = enviIndex();
		if (flowIndex == null)
			return Collections.emptyList();
		List<FlowValue> results = new ArrayList<>(flowIndex.size());
		flowIndex.each((i, f) -> results.add(
				new FlowValue(f, getTotalFlowResult(f))));
		return results;
	}

	/**
	 * Returns the total LCIA result $\mathbf{h}_i$ of the given LCIA category
	 * $i$.
	 */
	public double getTotalImpactResult(ImpactDescriptor impact) {
		var impactIndex = impactIndex();
		if (impactIndex == null)
			return 0;
		int idx = impactIndex.of(impact);
		var totalImpacts = totalImpactResults();
		if (idx < 0 || idx >= totalImpacts.length)
			return 0;
		return totalImpacts[idx];
	}

	/**
	 * Returns the impact category results for the given result.
	 */
	public List<ImpactValue> getTotalImpactResults() {
		var impactIndex = impactIndex();
		if (impactIndex == null)
			return Collections.emptyList();
		var results = new ArrayList<ImpactValue>();
		impactIndex.each((i, d) -> {
			double amount = getTotalImpactResult(d);
			results.add(new ImpactValue(d, amount));
		});
		return results;
	}

	/**
	 * Returns the sub-result for the given product. Sub-results are typically
	 * available for sub-systems of a product system.
	 *
	 * @param product the product for which the sub-result is requested
	 * @return the sub-result of the product. This is {@code null} if there is
	 * no such sub-result available in this result. Also, the returned result
	 * can be a more specific result (e.g. contribution result) depending on how
	 * the result was calculated.
	 */
	public LcaResult subResultOf(TechFlow product) {
		return subResults.get(product);
	}

	public Map<TechFlow, LcaResult> subResults() {
		return new HashMap<>(subResults);
	}

	public void addSubResult(TechFlow product, LcaResult result) {
		subResults.put(product, result);
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
			totalCosts(),
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
		return new UpstreamTree(this, totalCosts(),
			provider::totalCostsOfOne);
	}

	/**
	 * Calculate the upstream tree for the LCC result as added value.
	 */
	public UpstreamTree getAddedValueTree() {
		return new UpstreamTree(this, -totalCosts(),
			product -> -provider.totalCostsOfOne(product));
	}
}
