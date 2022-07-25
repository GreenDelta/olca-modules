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
	private final FullResult result;
	private double costs;

	private TagResult(String tag, FullResult result) {
		this.tag = tag;
		this.result = result;
		inventory = result.hasEnviFlows()
				? new double[result.totalFlowResults().length]
				: null;
		impacts = result.hasImpacts()
				? new double[result.totalImpactResults().length]
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

	public FlowValue inventoryResultOf(EnviFlow flow) {
		if (!hasInventoryResults())
			return new FlowValue(flow, 0);
		var idx = result.enviIndex().of(flow);
		double value = idx < 0
				? 0
				: result.adopt(flow, inventory[idx]);
		return new FlowValue(flow, value);
	}

	public ImpactValue impactResultOf(ImpactDescriptor impact) {
		if (!hasImpactResults())
			return new ImpactValue(impact, 0);
		var idx = result.impactIndex().of(impact);
		var value = idx < 0
				? 0
				: impacts[idx];
		return new ImpactValue(impact, value);
	}

	public double costs() {
		return costs;
	}

	private void addResultsOf(TechFlow techFlow) {
		var idx = result.techIndex().of(techFlow);
		if (idx < 0)
			return;
		if (hasInventoryResults()) {
			var b = result.provider().directFlowsOf(idx);
			for (int i = 0; i < b.length; i++) {
				inventory[i] += b[i];
			}
		}
		if (hasImpactResults()) {
			var h = result.provider().directImpactsOf(idx);
			for (int i = 0; i < h.length; i++) {
				impacts[i] += h[i];
			}
		}
		if (hasCosts) {
			costs += result.provider().directCostsOf(idx);
		}
	}

	private void addSubResult(double scaling, TagResult subResults) {
		if (hasInventoryResults() && subResults.hasInventoryResults()) {
			subResults.result.enviIndex().each((i, flow) -> {
				int idx = result.enviIndex().of(flow);
				if (idx >= 0) {
					inventory[idx] += scaling * subResults.inventory[i];
				}
			});
		}
		if (hasImpactResults() && subResults.hasImpactResults()) {
			subResults.result.impactIndex().each((i, impact) -> {
				int idx = result.impactIndex().of(impact);
				if (idx >= 0) {
					impacts[idx] += scaling * subResults.impacts[i];
				}
			});
		}
		if (hasCosts) {
			costs += scaling * subResults.costs;
		}
	}

	public static Collection<TagResult> allOf(FullResult result) {
		if (result == null)
			return Collections.emptyList();
		var tags = allTagsOf(result);
		if (tags.isEmpty())
			return Collections.emptyList();

		return tags.stream()
				.map(tag -> TagResult.of(tag, result))
				.collect(Collectors.toList());

	}

	public static TagResult of(String tag, FullResult result) {
		var tagResult = new TagResult(tag, result);
		for (var techFlow : result.techIndex()) {

			// add tag results of sub-systems recursively
			if (techFlow.isProductSystem()) {
				var subResult = result.subResultOf(techFlow);
				var subTags = TagResult.of(tag, subResult);
				var scaling = result.getScalingFactor(techFlow);
				tagResult.addSubResult(scaling, subTags);
				continue;
			}

			// add process results
			if (tagsOf(techFlow).contains(tag)) {
				tagResult.addResultsOf(techFlow);
			}
		}
		return tagResult;
	}

	private static Set<String> allTagsOf(FullResult result) {
		if (result == null)
			return Collections.emptySet();
		var tags = new HashSet<String>();
		for (var techFlow : result.techIndex()) {
			// add tags of sub-systems recursively
			if (techFlow.isProductSystem()) {
				var subResult = result.subResultOf(techFlow);
				tags.addAll(allTagsOf(subResult));
				continue;
			}

			// add tags of process
			tags.addAll(tagsOf(techFlow));
		}
		return tags;
	}

	private static Set<String> tagsOf(TechFlow techFlow) {
		var p = techFlow.provider();
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

	@Override
	public String toString() {
		var demand = result.demand();
		if (demand == null)
			return super.toString();
		return String.format(
				"TagResult '%s' for %.2f ref. units from '%s'",
				tag, demand.value(), demand.techFlow().provider().name);
	}
}
