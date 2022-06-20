package org.openlca.core.results;

import java.util.List;

import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;

/**
 * Describes a node in an upstream tree.
 *
 */
public class UpstreamNode {

	final TechFlow provider;

	/**
	 * The corresponding matrix index.
	 */
	final int index;

	double result;

	double requiredAmount;

	/**
	 * The scaling factor of this node.
	 */
	double scaling;

	/**
	 * The child nodes of this node. Node that the child nodes are calculated on
	 * demand and thus initialized with null.
	 */
	List<UpstreamNode> childs;

	private UpstreamNode(int index, TechFlow provider) {
		this.provider = provider;
		this.index = index;
	}

	static UpstreamNode rootOf(TechIndex techIndex, Demand demand) {
		var refFlow = demand.techFlow();
		int index = techIndex.of(refFlow);
		return new UpstreamNode(index, refFlow);
	}

	static UpstreamNode of(int index, TechIndex techIndex) {
		var flow =techIndex.at(index);
		return new UpstreamNode(index, flow);
	}

	/**
	 * Returns the provider of the product output or waste input of this upstream
	 * tree node.
	 */
	public TechFlow provider() {
		return provider;
	}

	/**
	 * Returns the upstream result of this node.
	 */
	public double result() {
		return result;
	}

	/**
	 * Returns the required amount of the provider flow of this upstream node.
	 */
	public double requiredAmount() {
		return requiredAmount;
	}
}
