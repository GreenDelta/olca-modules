package org.openlca.core.results;

import java.util.List;

import org.openlca.core.matrix.Provider;

/**
 * Describes a node in an upstream tree.
 * 
 */
public class UpstreamNode {

	/**
	 * A process-flow pair which is a product output or waste input that
	 * describes a process in the upstream result tree.
	 */
	public Provider provider;

	/**
	 * The upstream result of this node.
	 */
	public double result;

	/**
	 * The corresponding matrix index.
	 */
	int index;

	/**
	 * The scaling factor of this node.
	 */
	double scaling;

	/**
	 * The child nodes of this node. Node that the child nodes are calculated on
	 * demand and thus initialized with null.
	 */
	List<UpstreamNode> childs;

}
