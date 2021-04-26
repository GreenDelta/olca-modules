package org.openlca.core.matrix.linking;

/**
 * Indicates how default providers of product inputs or waste outputs in
 * processes should be considered in the linking of a product system.
 */
public enum ProviderLinking {

	/**
	 * Default provider settings are ignored in the linking process. This
	 * means that the linker can also select another provider even when a
	 * default provider is set.
	 */
	IGNORE_DEFAULTS,

	/**
	 * When a default provider is set for a product input or waste output
	 * the linker will always select this process. For other exchanges it
	 * will select the provider according to its other rules.
	 */
	PREFER_DEFAULTS,

	/**
	 * Means that links should be created only for product inputs or waste
	 * outputs where a default provider is defined which are then linked
	 * exactly to this provider.
	 */
	ONLY_DEFAULTS

}