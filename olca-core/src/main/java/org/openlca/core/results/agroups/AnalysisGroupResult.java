package org.openlca.core.results.agroups;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntToDoubleFunction;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.providers.ResultProvider;

public class AnalysisGroupResult {

	private final LcaResult result;
	private final Tree root;

	private AnalysisGroupResult(LcaResult result, Tree root) {
		this.result = result;
		this.root = root;
	}

	public static AnalysisGroupResult of(ProductSystem system, LcaResult result) {
		if (system == null
				|| system.analysisGroups.isEmpty()
				|| result == null)
			return new AnalysisGroupResult(result, null);

		var groups = GroupMap.of(result, system.analysisGroups);
		var graph = SubGraph.of(system, groups);
		var tree = Traversal.treeOf(result, groups, graph);
		return new AnalysisGroupResult(result, tree);
	}

	public boolean isEmpty() {
		return root == null
				|| root.childs() == null
				|| root.childs().isEmpty();
	}

	public Map<String, Double> getResultsOf(ImpactDescriptor impact) {
		if (isEmpty() || !result.hasImpacts())
			return Collections.emptyMap();
		int impactPos = result.impactIndex().of(impact);
		if (impactPos < 0)
			return Collections.emptyMap();
		var r = result.provider();
		return solve(index -> r.totalImpactOfOne(impactPos, index));
	}

	public Map<String, Double> getResultsOf(EnviFlow flow) {
		if (isEmpty() || !result.hasEnviFlows())
			return Collections.emptyMap();
		int flowPos = result.enviIndex().of(flow);
		if (flowPos < 0)
			return Collections.emptyMap();
		var r = result.provider();
		return solve(index -> ResultProvider.flowValueView(
				flow, r.totalFlowOfOne(flowPos, index)));
	}

	public Map<String, Double> getCostResults() {
		if (isEmpty() || !result.hasCosts())
			return Collections.emptyMap();
		var r = result.provider();
		return solve(r::totalCostsOfOne);
	}

	public Map<String, Double> getAddedValueResults() {
		if (isEmpty() || !result.hasCosts())
			return Collections.emptyMap();
		var r = result.provider();
		return solve(index -> -r.totalCostsOfOne(index));
	}

	private Map<String, Double> solve(IntToDoubleFunction intensity) {
		var map = new HashMap<String, Double>();
		map.put(
				root.group(),
				root.amount() * intensity.applyAsDouble(root.index()));
		var queue = new ArrayDeque<Tree>();
		queue.add(root);

		while (!queue.isEmpty()) {
			var parent = queue.poll();
			for (var next : parent.childs()) {
				queue.add(next);
				if (Objects.equals(parent.group(), next.group()))
					continue;
				double v = next.amount() * intensity.applyAsDouble(next.index());
				if (v == 0)
					continue;
				map.compute(parent.group(), ($, sum) -> sum != null ? sum - v : -v);
				map.compute(next.group(), ($, sum) -> sum != null ? sum + v : v);
			}
		}
		return map;
	}

}
