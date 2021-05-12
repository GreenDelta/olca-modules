package org.openlca.core.matrix.linking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.index.LongPair;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechFlowIndex;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.ProcessLink;
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
	public TechFlowIndex build(TechFlow refProduct) {
		return build(refProduct, 1.0);
	}

	@Override
	public TechFlowIndex build(TechFlow refFlow, double demand) {
		log.trace("build product index for {}", refFlow);
		TechFlowIndex index = new TechFlowIndex(refFlow);
		index.setDemand(demand);
		addSystemLinks(index);
		List<TechFlow> block = new ArrayList<>();
		block.add(refFlow);
		HashSet<TechFlow> handled = new HashSet<>();
		while (!block.isEmpty()) {
			List<TechFlow> nextBlock = new ArrayList<>();
			log.trace("fetch next block with {} entries", block.size());
			Map<Long, List<CalcExchange>> exchanges = fetchExchanges(block);
			for (TechFlow recipient : block) {
				handled.add(recipient);
				List<CalcExchange> all = exchanges.get(recipient.processId());
				List<CalcExchange> candidates = providers
						.getLinkCandidates(all);
				for (CalcExchange linkExchange : candidates) {
					TechFlow provider = providers.find(linkExchange);
					if (provider == null)
						continue;
					LongPair exchange = new LongPair(recipient.processId(),
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

	private void addSystemLinks(TechFlowIndex index) {
		if (system == null)
			return;
		for (ProcessLink link : system.processLinks) {
			TechFlow provider = providers.getProvider(
					link.providerId, link.flowId);
			if (provider == null)
				continue;
			LongPair exchange = new LongPair(link.processId, link.exchangeId);
			index.putLink(exchange, provider);
		}
	}

	private Map<Long, List<CalcExchange>> fetchExchanges(List<TechFlow> block) {
		if (block.isEmpty())
			return Collections.emptyMap();
		Set<Long> processIds = new HashSet<>();
		for (TechFlow provider : block) {
			processIds.add(provider.processId());
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
