package org.openlca.core.matrix;

import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;

public interface TechLinker {

	/**
	 * Get the linked provider for the given product input or waste output.
	 * You should never pass null into this method. Also make sure that the
	 * given exchange is linkable.
	 */
	TechFlow providerOf(CalcExchange e);

	/**
	 * The default linker gets the product index of a system (which may be the
	 * index of a complete database) and links exchanges by their default
	 * providers. If these exchanges do not have a default provider defined it
	 * links them by the exchange flow. BUT linking by flow only works when there
	 * is only a single provider for each product and waste flow in the system.
	 */
	class Default implements TechLinker {

		private final TechIndex techIndex;

		/**
		 * A map that assigns the IDs of products and waste flows to their
		 * respective providers. This map is initialized lazily when there are no
		 * default providers on product inputs or waste outputs. In this case, this
		 * matrix builder only works correctly when each product (waste) is only
		 * produced (treated) by a single process in the database.
		 */
		private TLongObjectHashMap<TechFlow> providers;

		private Default(TechIndex techIndex) {
			this.techIndex = techIndex;
		}

		public static TechLinker of(TechIndex idx) {
			return new Default(idx);
		}

		@Override
		public TechFlow providerOf(CalcExchange e) {
			if (e.defaultProviderId > 0) {
				var p = techIndex.getProvider(e.defaultProviderId, e.flowId);
				if (p != null)
					return p;
			}
			if (providers == null) {
				providers = new TLongObjectHashMap<>();
				techIndex.each(
					(i, pp) -> providers.put(pp.flowId(), pp));
			}
			return providers.get(e.flowId);
		}
	}
}
