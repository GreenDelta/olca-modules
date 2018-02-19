package org.openlca.core.matrix.product.index;

/**
 * Describes a link of a product input or waste output to a provider flow.
 */
class Link {

	/**
	 * The amount of the linked product input or waste output in the process.
	 */
	final double amount;

	/**
	 * The node that describes the provider of the product or waste treatment.
	 */
	final Node provider;

	/**
	 * The scaled amount that needs to be provided by the linked process.
	 */
	double demand;

	/** The ID of the respective exchange. */
	long exchangeId;

	Link(Node provider, long exchangeId, double amount, double demand) {
		this.provider = provider;
		this.exchangeId = exchangeId;
		this.amount = amount;
		this.demand = demand;
	}
}