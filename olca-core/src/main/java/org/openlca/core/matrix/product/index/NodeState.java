package org.openlca.core.matrix.product.index;

/**
 * The different states a node can take when building the product graph.
 */
enum NodeState {

	/**
	 * The node is in the waiting queue for the next iteration.
	 */
	WAITING,
	
	/**
	 * The node will be handled in the current iteration.
	 */
	PROGRESS,
	
	/**
	 * The node was handled in an iteration and all inputs were followed.
	 */
	FOLLOWED,
	
	/**
	 * The node was created in an iteration but it was excluded because its 
	 * demand is smaller than the cutoff.
	 */
	EXCLUDED,
	
	/**
	 * The node was handled in a current re-scaling process (and should be
	 * not rescaled again to avoid endless loops).
	 */
	RESCALED
	
}
