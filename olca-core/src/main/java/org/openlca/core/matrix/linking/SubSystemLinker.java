package org.openlca.core.matrix.linking;

import java.util.HashSet;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.cache.ExchangeTable;
import org.openlca.core.matrix.index.LongPair;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * A product system linker that tries to link to sub-systems first.
 */
public class SubSystemLinker implements ITechIndexBuilder {

	private final ProviderIndex providers;
	private final ExchangeTable exchanges;

	public SubSystemLinker(IDatabase db) {
		providers = ProviderIndex.lazy(db);
		exchanges = new ExchangeTable(db);
	}

	@Override
	public TechIndex build(TechFlow refFlow) {
		var index = new TechIndex(refFlow);

		if (!refFlow.isProcess())
			return index;

		var handled = new HashSet<Long>();
		var nextBlock = new HashSet<Long>();
		nextBlock.add(refFlow.providerId());

		while(!nextBlock.isEmpty()) {

			var linkables = exchanges.linkablesOf(nextBlock);
			handled.addAll(nextBlock);
			nextBlock.clear();

			for (var linkable : linkables) {
				var provider = providerOf(linkable, refFlow);
				if (provider == null)
					continue;
				var exchange = LongPair.of(linkable.processId(), linkable.exchangeId());
				index.putLink(exchange, provider);
				if (provider.isProcess()) {
					Long processId = provider.providerId();
					if (!handled.contains(processId)) {
						nextBlock.add(processId);
					}
				}
			}
		}

		return index;
	}

	private TechFlow providerOf(ExchangeTable.Linkable linkable, TechFlow refFlow) {
		var candidates = providers.getProvidersOf(linkable.flowId());
		TechFlow candidate = null;
		for (var next : candidates) {
				if (refFlow.equals(next))
					continue;
				if (next.isProductSystem())
					return next;
				if (isBetter(linkable, candidate, next)) {
					candidate = next;
				}
		}
		return candidate;
	}

	private boolean isBetter(ExchangeTable.Linkable linkable,
		TechFlow current, TechFlow next) {
		if (current == null)
			return true;

		// prefer default providers
		if (current.providerId() == linkable.providerId())
			return false;
		if (next.providerId() == linkable.providerId())
			return true;

		// prefer system processes
		if (current.provider() instanceof ProcessDescriptor p) {
			if (p.processType == ProcessType.LCI_RESULT)
				return false;
		}
		if (next.provider() instanceof ProcessDescriptor p) {
			return p.processType == ProcessType.LCI_RESULT;
		}

		return false;
	}
}
