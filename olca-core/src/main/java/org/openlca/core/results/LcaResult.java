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

	public double scalingFactorOf(TechFlow product) {
		int idx = techIndex().of(product);
		var scalingVector = provider.scalingVector();
		if (idx < 0 || idx > scalingVector.length)
			return 0;
		return scalingVector[idx];
	}

	public List<TechFlowValue> totalRequirements() {
		var index = techIndex();
		var list = new ArrayList<TechFlowValue>(index.size());
		var t = provider.totalRequirements();
		for (int i = 0; i < index.size(); i++) {
			var techFlow = index.at(i);
			var ti = t[i];
			double value = techFlow.isWaste() && ti != 0
					? -ti
					: ti;
			list.add(TechFlowValue.of(techFlow, value));
		}
		return list;
	}

	public double totalRequirementsOf(TechFlow techFlow) {
		int idx = provider.indexOf(techFlow);
		if (idx < 0)
			return 0;
		double value = provider.totalRequirementsOf(idx);
		return techFlow.isWaste() && value != 0
				? -value
				: value;
	}

	public List<EnviFlowValue> totalFlows() {
		var idx = enviIndex();
		if (idx == null)
			return Collections.emptyList();
		var list = new ArrayList<EnviFlowValue>(idx.size());
		var values = provider.totalFlows();
		for (int i = 0; i < idx.size(); i++) {
			var enviFlow = idx.at(i);
			var value = ResultProvider.flowValueView(enviFlow, values[i]);
			list.add(EnviFlowValue.of(enviFlow, value));
		}
		return list;
	}

	public double totalFlowOf(EnviFlow flow) {
		var idx = provider.indexOf(flow);
		if (idx < 0)
			return 0;
		var values = provider.totalFlows();
		var value = values[idx];
		return ResultProvider.flowValueView(flow, value);
	}

	public double totalCosts() {
		return provider.totalCosts();
	}

	public List<ImpactValue> totalImpacts() {
		var idx = impactIndex();
		if (idx == null)
			return Collections.emptyList();
		var values = provider.totalImpacts();
		var results = new ArrayList<ImpactValue>(idx.size());
		for (int i = 0; i < idx.size(); i++) {
			results.add(ImpactValue.of(idx.at(i), values[i]));
		}
		return results;
	}

	public double totalImpactOf(ImpactDescriptor impact) {
		int idx = provider.indexOf(impact);
		if (idx < 0)
			return 0;
		var values = provider.totalImpacts();
		return values[idx];
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

	public double directFlowOf(EnviFlow enviFlow, TechFlow techFlow) {
		int flowIdx = provider.indexOf(enviFlow);
		int techIdx = provider.indexOf(techFlow);
		if (flowIdx < 0 || techIdx < 0)
			return 0;
		double value = provider.directFlowOf(flowIdx, techIdx);
		return ResultProvider.flowValueView(enviFlow, value);
	}

	public List<EnviFlowValue> directFlowsOf(TechFlow techFlow) {
		if (!hasEnviFlows())
			return Collections.emptyList();
		int pos = provider.indexOf(techFlow);
		if (pos < 0)
			return Collections.emptyList();
		var values = provider.directFlowsOf(pos);
		var results = new ArrayList<EnviFlowValue>();
		enviIndex().each((i, flow) -> {
			double value = ResultProvider.flowValueView(flow, values[i]);
			results.add(new EnviFlowValue(flow, value));
		});
		return results;
	}

	public double directImpactOf(ImpactDescriptor impact, TechFlow techFlow) {
		int impactIdx = provider.indexOf(impact);
		int techIdx = provider.indexOf(techFlow);
		return impactIdx < 0 || techIdx < 0
				? 0
				: provider.directImpactOf(impactIdx, techIdx);
	}


	public List<ImpactValue> directImpactsOf(TechFlow techFlow) {
		var impactIdx = impactIndex();
		if (MatrixIndex.isAbsent(impactIdx))
			return Collections.emptyList();
		var results = new ArrayList<ImpactValue>();
		for (var impact : impactIdx) {
			var amount = directImpactOf(impact, techFlow);
			results.add(new ImpactValue(impact, amount));
		}
		return results;
	}

	public List<TechFlowValue> directCosts() {
		if (!hasCosts())
			return Collections.emptyList();
		var list = new ArrayList<TechFlowValue>();
		for (var techFlow : techIndex()) {
			list.add(TechFlowValue.of(techFlow, directCostsOf(techFlow)));
		}
		return list;
	}

	public double directCostsOf(TechFlow techFlow) {
		if (!hasCosts())
			return 0;
		int col = provider.indexOf(techFlow);
		return col < 0
				? 0
				: provider.directCostsOf(col);
	}

	/**
	 * Get the direct contributions of the processes in the system to the inventory
	 * result of the given flow.
	 */
	public List<Contribution<TechFlow>> getProcessContributions(EnviFlow flow) {
		return Contributions.calculate(
				techIndex(),
				totalFlowOf(flow),
				techFlow -> directFlowOf(flow, techFlow));
	}

	/**
	 * Get the direct contributions of the processes in the system to the LCIA
	 * result of the given LCIA category.
	 */
	public List<Contribution<TechFlow>> getProcessContributions(
			ImpactDescriptor impact) {
		return Contributions.calculate(
				techIndex(),
				totalImpactOf(impact),
				techFlow -> directImpactOf(impact, techFlow));
	}


	/**
	 * Get the direct contributions of all processes to the LCC result.
	 */
	public List<Contribution<TechFlow>> getProcessCostContributions() {
		return Contributions.calculate(
				techIndex(),
				totalCosts(),
				this::directCostsOf);
	}

	public double impactOfEnviFlow(ImpactDescriptor impact, EnviFlow flow) {
		int impactIdx = provider.indexOf(impact);
		int flowIdx = provider.indexOf(flow);
		return impactIdx < 0 || flowIdx < 0
				? 0
				: provider.flowImpactOf(impactIdx, flowIdx);
	}

	public List<EnviFlowValue> impactOfEnviFlows(ImpactDescriptor impact) {
		if (!hasEnviFlows())
			return Collections.emptyList();
		return enviIndex().stream().map(enviFlow -> {
			double value = impactOfEnviFlow(impact, enviFlow);
			return new EnviFlowValue(enviFlow, value);
		}).toList();
	}

	public double impactOfTechFlow(ImpactDescriptor impact, TechFlow techFlow) {
		int impactIdx = provider.indexOf(impact);
		int techIdx = provider.indexOf(techFlow);
		return impactIdx < 0 || techIdx < 0
				? 0
				: provider.directImpactOf(impactIdx, techIdx);
	}

	public List<TechFlowValue> impactOfTechFlows(ImpactDescriptor impact) {
		return techIndex().stream().map(techFlow -> {
			double value = directImpactOf(impact, techFlow);
			return new TechFlowValue(techFlow, value);
		}).toList();
	}

	public double impactFactorOf(ImpactDescriptor impact, EnviFlow flow) {
		int impactIdx = provider.indexOf(impact);
		int flowIdx = provider.indexOf(flow);
		if (impactIdx < 0 || flowIdx < 0)
			return 0;
		double value = provider.impactFactorOf(impactIdx, flowIdx);
		return ResultProvider.flowValueView(flow, value);
	}

	public double totalFlowOf(EnviFlow enviFlow, TechFlow techFlow) {
		int flowIdx = provider.indexOf(enviFlow);
		int techIdx = provider.indexOf(techFlow);
		if (flowIdx < 0 || techIdx < 0)
			return 0;
		double amount = provider.totalFlowOf(flowIdx, techIdx);
		return ResultProvider.flowValueView(enviFlow, amount);
	}


	public List<EnviFlowValue> totalFlowsOf(TechFlow techFlow) {
		var enviIdx = enviIndex();
		if (MatrixIndex.isAbsent(enviIdx))
			return Collections.emptyList();
		var list = new ArrayList<EnviFlowValue>();
		enviIdx.each((i, enviFlow) -> {
			double value = totalFlowOf(enviFlow, techFlow);
			list.add(new EnviFlowValue(enviFlow, value));
		});
		return list;
	}

	public double totalImpactOf(ImpactDescriptor impact, TechFlow techFlow) {
		int impactIdx = provider.indexOf(impact);
		int techIdx = provider.indexOf(techFlow);
		return impactIdx < 0 || techIdx < 0
				? 0
				: provider.totalImpactOf(impactIdx, techIdx);
	}

	public List<ImpactValue> totalImpactsOf(TechFlow techFlow) {
		var impactIdx = impactIndex();
		if (MatrixIndex.isAbsent(impactIdx))
			return Collections.emptyList();
		var list = new ArrayList<ImpactValue>();
		impactIndex().each((i, impact) -> {
			double amount = totalImpactOf(impact, techFlow);
			list.add(new ImpactValue(impact, amount));
		});
		return list;
	}

	public double totalCostsOf(TechFlow techFlow) {
		if (!this.hasCosts())
			return 0;
		int techIdx = provider.indexOf(techFlow);
		return techIdx < 0 ? 0 : provider.totalCostsOf(techIdx);
	}

	public List<TechFlowValue> totalCostsByTechFlow() {
		if (!this.hasCosts())
			return Collections.emptyList();
		var list = new ArrayList<TechFlowValue>();
		techIndex().each((i, techFlow) -> {
			list.add(new TechFlowValue(techFlow, totalCostsOf(techFlow)));
		});
		return list;
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
}
