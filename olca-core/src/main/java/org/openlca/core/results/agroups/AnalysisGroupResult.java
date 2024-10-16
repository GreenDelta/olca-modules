package org.openlca.core.results.agroups;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.LcaResult;

public class AnalysisGroupResult {

	private final LcaResult result;
	private final PathNode root;

	private AnalysisGroupResult(LcaResult result, PathNode root) {
		this.result = result;
		this.root = root;
	}

	public static AnalysisGroupResult of(ProductSystem system, LcaResult result) {
		if (system == null
				|| system.analysisGroups.isEmpty()
				|| result == null)
			return new AnalysisGroupResult(result, null);

		var groupMap = GroupMap.of(result, system.analysisGroups);
		var tree = new GroupTree(system, groupMap, result).build();
		return new AnalysisGroupResult(result, tree);
	}

	public boolean isEmpty() {
		return root == null
				|| root.branches() == null
				|| root.branches().isEmpty();
	}

	public Map<String, Double> groupResultsOf(ImpactDescriptor impact) {
		if (isEmpty() || !result.hasImpacts())
			return Collections.emptyMap();
		int impactPos = result.impactIndex().of(impact);
		if (impactPos < 0)
			return Collections.emptyMap();
		var r = result.provider();

		var map = new HashMap<String, Double>();
		map.put(root.group(), r.totalImpacts()[impactPos]);

		var queue = new ArrayDeque<PathNode>();
		queue.add(root);
		while (!queue.isEmpty()) {
			var parent = queue.poll();
			for (var next : parent.branches()) {
				queue.add(next);
				if (Objects.equals(parent.group(), next.group()))
					continue;
				double v = next.amount() * r.totalImpactOfOne(impactPos, next.index());
				if (v == 0)
					continue;
				map.compute(parent.group(), ($, sum) -> sum != null ? sum - v : -v);
				map.compute(next.group(), ($, sum) -> sum != null ? sum + v : v);
			}
		}
		return map;
	}

}
