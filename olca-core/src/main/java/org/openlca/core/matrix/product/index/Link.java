package org.openlca.core.matrix.product.index;

/**
 * Describes a product input.
 */
class Link {
	
	/**
	 * The input amount of the product in the receiving process (given in the
	 * reference unit and flow property).
	 */
	double inputAmount;
	
	/**
	 * The node that describes the provider of the input product.
	 */
	Node provider;
	
	Link(Node provider, double inputAmount) {
		this.provider = provider;
		this.inputAmount = inputAmount;
	}
}