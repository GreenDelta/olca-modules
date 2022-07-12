package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntToDoubleFunction;

import org.openlca.core.matrix.index.EnviFlow;

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

	private final IntToDoubleFunction intensity;
	private final FullResult r;

	public UpstreamTree(FullResult r, double total, IntToDoubleFunction intensity) {
		this(null, r, total, intensity);
	}

	public UpstreamTree(
		Object ref, FullResult r, double total, IntToDoubleFunction intensity) {
		this.ref = ref;
		this.r = r;
		this.intensity = intensity;
		root = UpstreamNode.rootOf( r.techIndex(), r.demand());
		double demand = r.demand().value();
		root.scaling = demand / r.provider.techValueOf(root.index, root.index);
		setRequiredAmount(root, demand);
		root.result = total;
	}

	public List<UpstreamNode> childs(UpstreamNode parent) {
		if (parent.childs != null)
			return parent.childs;
		parent.childs = new ArrayList<>();
		if (parent.scaling == 0)
			return parent.childs;

		var requirements = r.provider.techColumnOf(parent.index);
		for (int i = 0; i < requirements.length; i++) {
			if (i == parent.index)
				continue;
			double aij = requirements[i];
			if (aij == 0)
				continue;
			aij *= parent.scaling;
			double aii = r.provider.techValueOf(i, i);
			double scaling = -aij / aii;
			double amount = aii * scaling;

			var child = UpstreamNode.of(i, r.techIndex());
			child.scaling = scaling;
			setRequiredAmount(child, amount);
			child.result = adopt(intensity.applyAsDouble(i) * amount);
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
			? r.adopt(flow, value)
			: value;
	}

	private void setRequiredAmount(UpstreamNode child, double value) {
		if (value == 0)
			return;
		child.requiredAmount = child.provider.isWaste()
			? -value
			: value;
	}
}
