package org.openlca.core.matrix.linking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.index.LongPair;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.ProductSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TechIndexBuilder implements ITechIndexBuilder {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final ProviderSearch providers;
	private final MatrixCache cache;
	private final ProductSystem system;

	public TechIndexBuilder(MatrixCache cache, ProductSystem system,
			LinkingConfig config) {
		this.cache = cache;
		this.system = system;
		this.providers = new ProviderSearch(cache.getProcessTable(), config);
	}

	@Override
	public TechIndex build(TechFlow refFlow) {
		log.trace("build product index for {}", refFlow);
		var index = new TechIndex(refFlow);
		addSystemLinks(index);
		List<TechFlow> block = new ArrayList<>();
		block.add(refFlow);
		var handled = new HashSet<TechFlow>();
		while (!block.isEmpty()) {
			var nextBlock = new ArrayList<TechFlow>();
			log.trace("fetch next block with {} entries", block.size());
			Map<Long, List<CalcExchange>> exchanges = fetchExchanges(block);
			for (TechFlow recipient : block) {
				handled.add(recipient);
				List<CalcExchange> all = exchanges.get(recipient.providerId());
				List<CalcExchange> candidates = providers
						.getLinkCandidates(all);
				for (CalcExchange linkExchange : candidates) {
					TechFlow provider = providers.find(linkExchange);
					if (provider == null)
						continue;
					LongPair exchange = new LongPair(recipient.providerId(),
							linkExchange.exchangeId);
					index.putLink(exchange, provider);
					if (!handled.contains(provider)
							&& !nextBlock.contains(provider))
						nextBlock.add(provider);
				}
			}
			block = nextBlock;
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

	private Map<Long, List<CalcExchange>> fetchExchanges(List<TechFlow> block) {
		if (block.isEmpty())
			return Collections.emptyMap();
		Set<Long> processIds = new HashSet<>();
		for (TechFlow provider : block) {
			processIds.add(provider.providerId());
		}
		try {
			return cache.getExchangeCache().getAll(processIds);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to load exchanges from cache", e);
			return Collections.emptyMap();
		}
	}

}
