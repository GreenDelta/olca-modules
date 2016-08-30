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

	/** The ID of the product-input exchange. */
	long exchangeId;

	Link(Node provider, long exchangeId, double inputAmount, double demand) {
		this.provider = provider;
		this.exchangeId = exchangeId;
		this.inputAmount = inputAmount;
		this.demand = demand;
	}
}