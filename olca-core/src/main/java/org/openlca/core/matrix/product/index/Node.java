package org.openlca.core.matrix.product.index;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.matrix.LongPair;

import com.google.common.primitives.Doubles;

class Node implements Comparable<Node> {

	/**
	 * Indicates the state of the node when building the product graph.
	 */
	NodeState state;

	/**
	 * The product that is represented by this node (processId, flowId). There
	 * must be only one node for each product.
	 */
	LongPair product;

	/**
	 * The maximum demanded amount of product in the product system.
	 */
	double demand;

	/**
	 * The output amount of the product provided by the respective process.
	 */
	double outputAmount;

	/**
	 * The scaling factor of the process. The scaling factor is calculated via:
	 * 
	 * scalingFactor = demand / outputAmount.
	 * 
	 * It is used to calculate the demands of the input products to this node.
	 */
	double scalingFactor;

	/**
	 * The product inputs of this Node.
	 */
	List<Link> inputLinks = new ArrayList<>();

	Node(LongPair product, double demand) {
		this.product = product;
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
		return -Doubles.compare(this.demand, other.demand);
	}
}