package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntToDoubleFunction;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.providers.ResultProvider;

/**
 * Maps the upstream results of the product system graph to a tree where the
 * root is the reference process of the product system.
 */
public class UpstreamTree {

	public final UpstreamNode root;

	/**
	 * An optional reference to a model (e.g. flow or LCIA category) to which
	 * the upstream tree is related.
	 */
	public final Object ref;

	private final ResultProvider r;
	private final IntToDoubleFunction intensity;
	private final IntToDoubleFunction directResult;

	private UpstreamTree(
			Object ref,
			ResultProvider r,
			double total,
			IntToDoubleFunction intensity,
			IntToDoubleFunction directResult) {
		this.ref = ref;
		this.r = r;
		this.intensity = intensity;
		this.directResult = directResult;
		root = UpstreamNode.rootOf(r.techIndex(), r.demand());
		double demand = r.demand().value();
		root.scaling = demand / r.techValueOf(root.index, root.index);
		setRequiredAmount(root, demand);
		root.result = total;
		setDirectResult(root);
	}

	public static UpstreamTree of(ResultProvider provider, EnviFlow flow) {
		int flowIdx = provider.indexOf(flow);
		double total = ResultProvider.flowValueView(
				flow, provider.totalFlows()[flowIdx]);
		return new UpstreamTree(
				flow, provider, total,
				techIdx -> provider.totalFlowOfOne(flowIdx, techIdx),
				techIdx -> {
					var direct = provider.directFlowOf(flowIdx, techIdx);
					return ResultProvider.flowValueView(flow, direct);
				});
	}

	public static UpstreamTree of(ResultProvider provider, ImpactDescriptor impact) {
		int impactIdx = provider.indexOf(impact);
		double total = provider.totalImpacts()[impactIdx];
		return new UpstreamTree(
				impact, provider, total,
				techIdx -> provider.totalImpactOfOne(impactIdx, techIdx),
				techIdx -> provider.directImpactOf(impactIdx, techIdx));
	}

	public static UpstreamTree costsOf(ResultProvider provider) {
		return new UpstreamTree(
				null, provider, provider.totalCosts(),
				provider::totalCostsOfOne,
				provider::directCostsOf);
	}

	public static UpstreamTree addedValuesOf(ResultProvider provider) {
		return new UpstreamTree(
				null, provider, -provider.totalCosts(),
				techIdx -> -provider.totalCostsOfOne(techIdx),
				techIdx -> -provider.directCostsOf(techIdx));
	}

	public List<UpstreamNode> childs(UpstreamNode parent) {
		if (parent.childs != null)
			return parent.childs;
		parent.childs = new ArrayList<>();
		if (parent.scaling == 0)
			return parent.childs;

		var requirements = r.techColumnOf(parent.index);
		for (int i = 0; i < requirements.length; i++) {
			if (i == parent.index)
				continue;
			double aij = requirements[i];
			if (aij == 0)
				continue;
			aij *= parent.scaling;
			double aii = r.techValueOf(i, i);
			double scaling = -aij / aii;
			double amount = aii * scaling;

			var child = UpstreamNode.of(i, r.techIndex());
			child.scaling = scaling;
			setRequiredAmount(child, amount);
			child.result = adopt(intensity.applyAsDouble(i) * amount);
			setDirectResult(child);
			parent.childs.add(child);
		}

		parent.childs.sort((n1, n2) -> Double.compare(n2.result, n1.result));
		return parent.childs;
	}

	/**
	 * When the reference of this upstream tree is an input tree we have to
	 * switch the sign of it.
	 */
	private double adopt(double value) {
		if (value == 0)
			return 0;
		return ref instanceof EnviFlow flow
				? ResultProvider.flowValueView(flow, value)
				: value;
	}

	private void setRequiredAmount(UpstreamNode child, double value) {
		if (value == 0)
			return;
		child.requiredAmount = child.provider.isWaste()
				? -value
				: value;
	}

	private void setDirectResult(UpstreamNode node) {
		if (node.result == 0)
			return;
		var resultScaling = r.scalingFactorOf(node.index);
		if (resultScaling == 0)
			return;
		double direct = directResult.applyAsDouble(node.index);
		if (direct == 0)
			return;
		node.direct = node.scaling * direct / resultScaling;
	}
}
