package org.openlca.core.matrix.linking;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.matrix.index.TechFlow;

/**
 * When building a product system graph, a node represents a product output or
 * waste input of a process.
 */
class Node implements Comparable<Node> {

	/**
	 * Indicates the state of the node when building the product graph.
	 */
	NodeState state;

	/**
	 * The product output or waste input that is represented by this node
	 * (processId, flowId). There must be only one node for each product output
	 * or waste input in a graph.
	 *
	 * TODO: update doc
	 */
	TechFlow flow;

	/**
	 * The maximum demanded amount of product in the product system.
	 */
	double demand;

	/**
	 * The amount of the respective product output or waste input.
	 */
	double amount;

	/**
	 * The scaling factor of the process. The scaling factor is calculated via:
	 *
	 * scalingFactor = demand / amount.
	 *
	 * It is used to calculate the demands of input products and waste outputs
	 * of this node.
	 */
	double scalingFactor;

	/**
	 * The product inputs or waste outputs that are linked to the provider flow
	 * (product output or waste input) of this node.
	 */
	List<Link> links = new ArrayList<>();

	Node(TechFlow flow, double demand) {
		this.flow = flow;
		this.demand = demand;
		state = NodeState.WAITING;
	}

	/**
	 * We need to handle nodes with higher demands first when building the
	 * product graph to minimize re-scaling.
	 */
	@Override
	public int compareTo(Node other) {
		if (other == null)
			return 0;
		return -Double.compare(Math.abs(this.demand),
				Math.abs(other.demand));
	}
}
