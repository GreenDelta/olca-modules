package org.openlca.core.matrix.linking;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.cache.ExchangeTable;
import org.openlca.core.matrix.index.ProviderIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;

public class SubSystemLinker implements ITechIndexBuilder {

	private final ProviderIndex providers;
	private final ExchangeTable exchanges;

	public SubSystemLinker(IDatabase db) {
		providers = ProviderIndex.lazy(db);
		exchanges = new ExchangeTable(db);
	}

	@Override
	public TechIndex build(TechFlow refFlow) {
		TechIndex index = new TechIndex(refFlow);
		index.setDemand(1.0);



		return null;
	}

}
