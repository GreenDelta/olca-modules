package org.openlca.core.results;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.util.Strings;

public class TagResult {

	private final String tag;
	private final double[] inventory;
	private final double[] impacts;
	private final boolean hasCosts;
	private final ContributionResult result;
	private double costs;

	private TagResult(String tag, ContributionResult result) {
		this.tag = tag;
		this.result = result;
		inventory = result.hasEnviFlows()
			? new double[result.totalFlowResults.length]
			: null;
		impacts = result.hasImpacts()
			? new double[result.totalImpactResults.length]
			: null;
		hasCosts = result.hasCosts();
	}

	public String tag() {
		return tag;
	}

	public boolean hasInventoryResults() {
		return inventory != null;
	}

	public boolean hasImpactResults() {
		return impacts != null;
	}

	public boolean hasCostResults() {
		return hasCosts;
	}

	public FlowResult inventoryResultOf(EnviFlow flow) {
		if (!hasInventoryResults())
			return new FlowResult(flow, 0);
		var idx = result.enviIndex().of(flow);
		double value = idx < 0
			? 0
			: result.adopt(flow, inventory[idx]);
		return new FlowResult(flow, value);
	}

	public ImpactResult impactResultOf(ImpactDescriptor impact) {
		if (!hasImpactResults())
			return new ImpactResult(impact, 0);
		var idx = result.impactIndex().of(impact);
		var value = idx < 0
			? 0
			: impacts[idx];
		return new ImpactResult(impact, value);
	}

	public double costs() {
		return costs;
	}

	private void addResultsOf(TechFlow techFlow) {
		var idx = result.techIndex().of(techFlow);
		if (idx < 0)
			return;
		if (hasInventoryResults()) {
			var b = result.provider.directFlowsOf(idx);
			for (int i = 0; i < b.length; i++) {
				inventory[i] += b[i];
			}
		}
		if (hasImpactResults()) {
			var h = result.provider.directImpactsOf(idx);
			for (int i = 0; i < h.length; i++) {
				impacts[i] += h[i];
			}
		}
		if (hasCosts) {
			costs += result.provider.directCostsOf(idx);
		}
	}

	public static Collection<TagResult> allOf(ContributionResult result) {
		if (result == null)
			return Collections.emptyList();
		var tags = allTagsOf(result);
		if (tags.isEmpty())
			return Collections.emptyList();

		return tags.stream()
			.map(tag -> TagResult.of(tag, result))
			.collect(Collectors.toList());

	}

	public static TagResult of(String tag, ContributionResult result) {
		var tagResult = new TagResult(tag, result);
		for (var techFlow : result.techIndex()) {
			if (tagsOf(techFlow).contains(tag)) {
				tagResult.addResultsOf(techFlow);
			}
		}
		return tagResult;
	}

	private static Set<String> allTagsOf(ContributionResult result) {
		if (result == null)
			return Collections.emptySet();
		var tags = new HashSet<String>();
		for (var techFlow : result.techIndex()) {
			tags.addAll(tagsOf(techFlow));
		}
		return tags;
	}

	private static Set<String> tagsOf(TechFlow techFlow) {
		var p = techFlow.process();
		if (p == null || Strings.nullOrEmpty(p.tags))
			return Collections.emptySet();
		var set = new HashSet<String>();
		for (var t : p.tags.split(",")) {
			var tag = t.trim();
			if (!tag.isEmpty()) {
				set.add(tag);
			}
		}
		return set;
	}
}
