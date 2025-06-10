package org.openlca.core.matrix.linking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.cache.ExchangeTable;
import org.openlca.core.matrix.cache.ExchangeTable.Linkable;
import org.openlca.core.matrix.cache.ProviderMap;
import org.openlca.core.matrix.index.LongPair;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.ProductSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TechIndexBuilder implements ITechIndexBuilder {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final ProductSystem system;
	private final ProviderSearch providers;
	private final ExchangeTable exchanges;

	public TechIndexBuilder(
			IDatabase db, ProductSystem system, LinkingConfig config
	) {
		this.system = system;
		this.providers = new ProviderSearch(ProviderMap.create(db), config);
		this.exchanges = new ExchangeTable(db);
	}

	@Override
	public TechIndex build(TechFlow refFlow) {

		log.trace("build product index for {}", refFlow);
		var index = new TechIndex(refFlow);
		addSystemLinks(index);

		var block = new ArrayList<TechFlow>();
		block.add(refFlow);
		var handled = new HashSet<TechFlow>();

		while (!block.isEmpty()) {
			log.trace("fetch next block with {} entries", block.size());

			var linkables = linkablesOf(block);
			handled.addAll(block);
			block.clear();

			for (var linkable : linkables) {
				TechFlow provider = providers.find(linkable);
				if (provider == null)
					continue;
				var exchange = new LongPair(
						linkable.processId(), linkable.exchangeId());
				index.putLink(exchange, provider);
				if (!handled.contains(provider)	&& !block.contains(provider))
					block.add(provider);
			}
		}
		return index;
	}

	private void addSystemLinks(TechIndex index) {
		if (system == null)
			return;
		for (var link : system.processLinks) {
			var provider = providers.getProvider(link.providerId, link.flowId);
			if (provider == null)
				continue;
			var exchange = LongPair.of(link.processId, link.exchangeId);
			index.putLink(exchange, provider);
		}
	}

	private List<Linkable> linkablesOf(List<TechFlow> block) {
		if (block.isEmpty())
			return Collections.emptyList();
		var ids = new HashSet<Long>();
		for (var provider : block) {
			ids.add(provider.providerId());
		}
		return exchanges.linkablesOf(ids);
	}

}
