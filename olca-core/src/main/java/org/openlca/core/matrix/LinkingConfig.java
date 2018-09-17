package org.openlca.core.matrix;

import org.openlca.core.model.ProcessType;

public class LinkingConfig {

	public ProcessType preferredType = ProcessType.LCI_RESULT;
	public DefaultProviders providerLinking = DefaultProviders.PREFER;
	public Double cutoff;
	public LinkingCallback callback;

	/**
	 * Indicates how default providers of product inputs or waste outputs should
	 * be considered in the linking process.
	 */
	public enum DefaultProviders {

		/**
		 * Default provider settings are ignored in the linking process. This
		 * means that the linker can also select another provider even when a
		 * default provider is set.
		 */
		IGNORE,

		/**
		 * When a default provider is set for a product input or waste output
		 * the linker will always select this process. For other exchanges it
		 * will select the provider according to its other rules.
		 */
		PREFER,

		/**
		 * Means that links should be created only for product inputs or waste
		 * outputs where a default provider is defined which are then linked
		 * exactly to this provider.
		 */
		ONLY

	}
}
