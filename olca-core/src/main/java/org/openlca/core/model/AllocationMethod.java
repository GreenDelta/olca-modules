package org.openlca.core.model;

public enum AllocationMethod {

	/**
	 * This is a flag, that is used for calculations only. It means that the
	 * respective default allocation method of a process should be used when
	 * building a product system. Note, that this is not a valid value for the
	 * default allocation method in a process.
	 */
	USE_DEFAULT,

	/**
	 * Causal allocation means that the user can define an allocation factor for
	 * each input or output separately per output product of a process.
	 */
	CAUSAL,

	/**
	 * Means that an economic flow property is used to calculate the allocation
	 * factors of a process.
	 */
	ECONOMIC,

	/**
	 * Means that no allocation method should be used.
	 */
	NONE,

	/**
	 * Means that a physical flow property is used to calculate the allocation
	 * factors of a process.
	 */
	PHYSICAL;

}
