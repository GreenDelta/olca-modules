package org.openlca.core.matrix.product.index;

/**
 * Describes a product input.
 */
class Link {
	
	/**
	 * The input amount of the product in the receiving process (given in the
	 * reference unit and flow property).
	 */
	final double inputAmount;
	
	/**
	 * The node that describes the provider of the input product.
	 */
	final Node provider;
	
	double demand;
	
	Link(Node provider, double inputAmount, double demand) {
		this.provider = provider;
		this.inputAmount = inputAmount;
		this.demand = demand;
	}
}