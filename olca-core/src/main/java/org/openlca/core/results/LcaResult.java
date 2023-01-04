package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntToDoubleFunction;

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
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.providers.ResultProvider;
import org.openlca.core.results.providers.ResultProviders;
import org.openlca.core.results.providers.SolverContext;

/**
 * An LcaResult wraps a result provider and provides more friendly methods for
 * retrieving results from that provider. For getting more low-level results,
 * the provider can also be directly accessed.
 */
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

	// region: tech-flows

	public List<TechFlowValue> scalingFactors() {
		var s = provider.scalingVector();
		return techValuesOf((i, techFlow) -> s[i]);
	}

	public double scalingFactorOf(TechFlow product) {
		int idx = techIndex().of(product);
		var scalingVector = provider.scalingVector();
		if (idx < 0 || idx > scalingVector.length)
			return 0;
		return scalingVector[idx];
	}

	public List<TechFlowValue> totalityFactors() {
		return techValuesOf((i, techFlow) -> provider.totalFactorOf(i));
	}

	public double totalityFactorOf(TechFlow techFlow) {
		int idx = techIndex().of(techFlow);
		return  idx >= 0
				? provider.totalFactorOf(idx)
				: 0;
	}

	public List<TechFlowValue> totalRequirements() {
		var t = provider.totalRequirements();
		return techValuesOf((i, techFlow) -> t[i]);
	}

	public double totalRequirementsOf(TechFlow techFlow) {
		int idx = provider.indexOf(techFlow);
		return idx >= 0
				? provider.totalRequirementsOf(idx)
				: 0;
	}

	public List<TechFlowValue> directRequirementsOf(TechFlow techFlow) {
		int j = techIndex().of(techFlow);
		if (j < 0)
			return Collections.emptyList();
		return techValuesOf((i, other) -> provider.scaledTechValueOf(i, j));
	}

	public List<TechFlowValue> unscaledRequirementsOf(TechFlow techFlow) {
		int j = techIndex().of(techFlow);
		if (j < 0)
			return Collections.emptyList();
		return techValuesOf((i, other) -> provider.techValueOf(i, j));
	}

	// endregion

	// region: flows

	public List<EnviFlowValue> getTotalFlows() {
		if (!hasEnviFlows())
			return Collections.emptyList();
		var g = provider.totalFlows();
		return enviViewsOf((i, $) -> g[i]);
	}

	public double getTotalFlowValueOf(EnviFlow flow) {
		return enviViewOf(flow, i -> provider.totalFlows()[i]);
	}

	public List<TechFlowValue> getDirectFlowValuesOf(EnviFlow enviFlow) {
		int flowIdx = provider.indexOf(enviFlow);
		if (flowIdx < 0)
			return Collections.emptyList();
		return techValuesOf((techIdx, $) -> {
			double raw = provider.directFlowOf(flowIdx, techIdx);
			return ResultProvider.flowValueView(enviFlow, raw);
		});
	}

	public List<TechFlowValue> getTotalFlowValuesOf(EnviFlow enviFlow) {
		int flowIdx = provider.indexOf(enviFlow);
		if (flowIdx < 0)
			return Collections.emptyList();
		return techValuesOf((techIdx, $) -> {
			double raw = provider.totalFlowOf(flowIdx, techIdx);
			return ResultProvider.flowValueView(enviFlow, raw);
		});
	}

	public List<EnviFlowValue> getDirectFlowsOf(TechFlow techFlow) {
		if (!hasEnviFlows())
			return Collections.emptyList();
		var techIdx = provider.indexOf(techFlow);
		var values = provider.directFlowsOf(techIdx);
		return enviViewsOf((enviIdx, $) -> values[enviIdx]);
	}

	public double getDirectFlowOf(EnviFlow enviFlow, TechFlow techFlow) {
		int techIdx = provider.indexOf(techFlow);
		if (techIdx < 0)
			return 0;
		return enviViewOf(enviFlow,
				enviIdx -> provider.directFlowOf(enviIdx, techIdx));
	}

	public List<EnviFlowValue> getTotalFlowsOfOne(TechFlow techFlow) {
		int techIdx = provider.indexOf(techFlow);
		if (techIdx < 0 || !hasEnviFlows())
			return Collections.emptyList();
		var values = provider.totalFlowsOfOne(techIdx);
		return enviViewsOf((enviIdx, $) -> values[enviIdx]);
	}

	public double getTotalFlowOfOne(EnviFlow enviFlow, TechFlow techFlow) {
		var techIdx = provider.indexOf(techFlow);
		if (techIdx < 0)
			return 0;
		return enviViewOf(enviFlow,
				enviIdx -> provider.totalFlowOfOne(enviIdx, techIdx));
	}

	public List<EnviFlowValue> getTotalFlowsOf(TechFlow techFlow) {
		var techIdx = provider.indexOf(techFlow);
		if (techIdx < 0)
			return Collections.emptyList();
		var values = provider.totalFlowsOf(techIdx);
		return enviViewsOf((enviIdx, $) -> values[enviIdx]);
	}

	public double getTotalFlowOf(EnviFlow enviFlow, TechFlow techFlow) {
		int techIdx = provider.indexOf(techFlow);
		if (techIdx < 0)
			return 0;
		return enviViewOf(enviFlow,
				enviIdx -> provider.totalFlowOf(enviIdx, techIdx));
	}

	// endregion

	// region: impact assessment results

	public List<ImpactValue> getTotalImpacts() {
		if (!hasImpacts())
			return Collections.emptyList();
		var values = provider.totalImpacts();
		return impactValuesOf((impactIdx, $) -> values[impactIdx]);
	}

	public double getTotalImpactValueOf(ImpactDescriptor impact) {
		int idx = provider.indexOf(impact);
		if (idx < 0)
			return 0;
		var values = provider.totalImpacts();
		return values[idx];
	}

	public List<TechFlowValue> getDirectImpactValuesOf(ImpactDescriptor impact) {
		int impactIdx = provider.indexOf(impact);
		if (impactIdx < 0)
			return Collections.emptyList();
		return techValuesOf((techIdx, _techFlow) ->
				provider.directImpactOf(impactIdx, techIdx));
	}

	public List<TechFlowValue> getTotalImpactValuesOf(ImpactDescriptor impact) {
		int impactIdx = provider.indexOf(impact);
		if (impactIdx < 0)
			return Collections.emptyList();
		return techValuesOf((techIdx, _techFlow) ->
				provider.totalImpactOf(impactIdx, techIdx));
	}

	public List<ImpactValue> getDirectImpactsOf(TechFlow techFlow) {
		int techIdx = provider.indexOf(techFlow);
		if (techIdx < 0 || !hasImpacts())
			return Collections.emptyList();
		var values = provider.directImpactsOf(techIdx);
		return impactValuesOf((impactIdx, $) -> values[impactIdx]);
	}

	public double getDirectImpactOf(ImpactDescriptor impact, TechFlow techFlow) {
		int impactIdx = provider.indexOf(impact);
		int techIdx = provider.indexOf(techFlow);
		return impactIdx < 0 || techIdx < 0
				? 0
				: provider.directImpactOf(impactIdx, techIdx);
	}

	public List<ImpactValue> getTotalImpactsOfOne(TechFlow techFlow) {
		int techIdx = provider.indexOf(techFlow);
		if (techIdx < 0 || !hasImpacts())
			return Collections.emptyList();
		var values = provider.totalImpactsOfOne(techIdx);
		return impactValuesOf((impactIdx, $) -> values[impactIdx]);
	}

	public double getTotalImpactOfOne(ImpactDescriptor impact, TechFlow techFlow) {
		int impactIdx = provider.indexOf(impact);
		int techIdx = provider.indexOf(techFlow);
		return impactIdx < 0 || techIdx < 0
				? 0
				: provider.totalImpactOfOne(impactIdx, techIdx);
	}

	public List<ImpactValue> getTotalImpactsOf(TechFlow techFlow) {
		int techIdx = provider.indexOf(techFlow);
		if (techIdx < 0)
			return Collections.emptyList();
		var values = provider.totalImpactsOf(techIdx);
		return impactValuesOf((impactIdx, $) -> values[impactIdx]);
	}

	public double getTotalImpactOf(ImpactDescriptor impact, TechFlow techFlow) {
		int impactIdx = provider.indexOf(impact);
		int techIdx = provider.indexOf(techFlow);
		return impactIdx < 0 || techIdx < 0
				? 0
				: provider.totalImpactOf(impactIdx, techIdx);
	}

	public List<EnviFlowValue> getImpactFactorsOf(ImpactDescriptor impact) {
		int impactIdx = provider.indexOf(impact);
		if (impactIdx < 0)
			return Collections.emptyList();
		return enviViewsOf(
				(enviIdx, $) -> provider.impactFactorOf(impactIdx, enviIdx));
	}

	public double getImpactFactorOf(ImpactDescriptor impact, EnviFlow enviFlow) {
		int impactIdx = provider.indexOf(impact);
		if (impactIdx < 0)
			return 0;
		return enviViewOf(
				enviFlow, enviIdx -> provider.impactFactorOf(impactIdx, enviIdx));
	}

	public List<ImpactValue> getFlowImpactsOfOne(EnviFlow enviFlow) {
		int enviIdx = provider.indexOf(enviFlow);
		if (enviIdx < 0)
			return Collections.emptyList();
		return impactValuesOf(
				(impactIdx, $) -> {
					double raw = provider.impactFactorOf(impactIdx, enviIdx);
					return ResultProvider.flowValueView(enviFlow, raw);
				});
	}

	public List<ImpactValue> getFlowImpactsOf(EnviFlow enviFlow) {
		int enviIdx = provider.indexOf(enviFlow);
		if (enviIdx < 0)
			return Collections.emptyList();
		return impactValuesOf(
				(impactIdx, $) -> provider.flowImpactOf(impactIdx, enviIdx));
	}

	public double getFlowImpactOf(ImpactDescriptor impact, EnviFlow enviFlow) {
		int impactIdx = provider.indexOf(impact);
		int flowIdx = provider.indexOf(enviFlow);
		return impactIdx < 0 || flowIdx < 0
				? 0
				: provider.flowImpactOf(impactIdx, flowIdx);
	}

	public List<EnviFlowValue> getFlowImpactValuesOf(ImpactDescriptor impact) {
		int impactIdx = provider.indexOf(impact);
		if (impactIdx < 0)
			return Collections.emptyList();
		return enviValuesOf(
				(enviIdx, $) -> provider.flowImpactOf(impactIdx, enviIdx));
	}

	// endregion

	// region: costs

	/* TODO: not yet supported by provider API
	public List<TechFlowValue> getUnscaledCosts() {
		return techValuesOf((techIdx, $) -> provider.unscaledCostsOf(techIdx));
	}
	*/

	public double getTotalCosts() {
		return provider.totalCosts();
	}

	public List<TechFlowValue> getDirectCostValues() {
		if (!hasCosts())
			return Collections.emptyList();
		return techValuesOf((techIdx, $) -> provider.directCostsOf(techIdx));
	}

	public List<TechFlowValue> getTotalCostValues() {
		if (!hasCosts())
			return Collections.emptyList();
		return techValuesOf((techIdx, $) -> provider.totalCostsOf(techIdx));
	}

	public double getDirectCostsOf(TechFlow techFlow) {
		if (!hasCosts())
			return 0;
		int techIdx = provider.indexOf(techFlow);
		return techIdx < 0 ? 0 : provider.directCostsOf(techIdx);
	}

	public double getTotalCostsOfOne(TechFlow techFlow) {
		if (!this.hasCosts())
			return 0;
		int techIdx = provider.indexOf(techFlow);
		return techIdx < 0 ? 0 : provider.totalCostsOfOne(techIdx);
	}

	public double getTotalCostsOf(TechFlow techFlow) {
		if (!this.hasCosts())
			return 0;
		int techIdx = provider.indexOf(techFlow);
		return techIdx < 0 ? 0 : provider.totalCostsOf(techIdx);
	}

	// endregion

	// region: sub-results

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

	// endregion

	/**
	 * Get the direct contributions of the processes in the system to the inventory
	 * result of the given flow.
	 */
	@Deprecated
	public List<Contribution<TechFlow>> getProcessContributions(EnviFlow flow) {
		return Contributions.calculate(
				techIndex(),
				getTotalFlowValueOf(flow),
				techFlow -> getDirectFlowOf(flow, techFlow));
	}

	/**
	 * Get the direct contributions of the processes in the system to the LCIA
	 * result of the given LCIA category.
	 */
	@Deprecated
	public List<Contribution<TechFlow>> getProcessContributions(
			ImpactDescriptor impact) {
		return Contributions.calculate(
				techIndex(),
				getTotalImpactValueOf(impact),
				techFlow -> getDirectImpactOf(impact, techFlow));
	}

	/**
	 * Get the direct contributions of all processes to the LCC result.
	 */
	@Deprecated
	public List<Contribution<TechFlow>> getProcessCostContributions() {
		return Contributions.calculate(
				techIndex(),
				getTotalCosts(),
				this::getDirectCostsOf);
	}

	/**
	 * Get the contribution share of the outgoing process product (provider) to the
	 * product input (recipient) of the given link and the calculated product
	 * system. The returned share is a value between 0 and 1.
	 */
	public double linkShareOf(ProcessLink link) {

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

	// region utils

	private List<TechFlowValue> techValuesOf(IxVal<TechFlow> fn) {
		var list = new ArrayList<TechFlowValue>(techIndex().size());
		techIndex().each((i, techFlow) -> {
			double value = fn.call(i, techFlow);
			list.add(new TechFlowValue(techFlow, value));
		});
		return list;
	}

	private List<EnviFlowValue> enviViewsOf(IxVal<EnviFlow> fn) {
		return enviValuesOf((enviIdx, enviFlow) ->
				ResultProvider.flowValueView(enviFlow, fn.call(enviIdx, enviFlow)));
	}

	private List<EnviFlowValue> enviValuesOf(IxVal<EnviFlow> fn) {
		var enviIdx = enviIndex();
		if (MatrixIndex.isAbsent(enviIdx))
			return Collections.emptyList();
		var list = new ArrayList<EnviFlowValue>(enviIdx.size());
		enviIdx.each((i, enviFlow) -> {
			double value = fn.call(i, enviFlow);
			list.add(new EnviFlowValue(enviFlow, value));
		});
		return list;
	}

	private double enviViewOf(EnviFlow enviFlow, IntToDoubleFunction fn) {
		int i = provider().indexOf(enviFlow);
		return i >= 0
				? ResultProvider.flowValueView(enviFlow, fn.applyAsDouble(i))
				: 0;
	}

	private List<ImpactValue> impactValuesOf(IxVal<ImpactDescriptor> fn) {
		var impactIdx = impactIndex();
		if (MatrixIndex.isAbsent(impactIdx))
			return Collections.emptyList();
		var list = new ArrayList<ImpactValue>(impactIdx.size());
		impactIdx.each((i, impact) -> {
			double value = fn.call(i, impact);
			list.add(new ImpactValue(impact, value));
		});
		return list;
	}

	@FunctionalInterface
	interface IxVal<T> {
		double call(int i, T elem);
	}

	// endregion
}
