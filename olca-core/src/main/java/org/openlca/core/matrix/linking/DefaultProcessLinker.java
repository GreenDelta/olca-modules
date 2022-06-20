package org.openlca.core.matrix.linking;

import java.util.HashSet;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.cache.ExchangeTable;
import org.openlca.core.matrix.index.LongPair;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class DefaultProcessLinker implements ITechIndexBuilder {

	private final ProviderIndex providers;
	private final ExchangeTable exchanges;

	private DefaultProcessLinker(LinkingInfo linking) {
		this.providers = ProviderIndex.of(linking);
		this.exchanges = new ExchangeTable(linking.db());
	}

	public static DefaultProcessLinker of(IDatabase db) {
		return new DefaultProcessLinker(LinkingInfo.of(db));
	}

	public static DefaultProcessLinker of(LinkingInfo linking) {
		return new DefaultProcessLinker(linking);
	}

	@Override
	public TechIndex build(TechFlow refFlow) {
		TechIndex index = new TechIndex(refFlow);
		if (!refFlow.isProcess())
			return index;

		var handled = new HashSet<Long>();
		var nextBlock = new HashSet<Long>();
		nextBlock.add(refFlow.providerId());

		while (!nextBlock.isEmpty()) {

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
			if (candidate == null) {
				candidate = next;
				continue;
			}
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

		// prefer processes to product systems
		if (current.isProcess() && !next.isProcess())
			return false;
		if (next.isProcess() && !current.isProcess())
			return true;

		// compare processes
		if ((current.provider() instanceof ProcessDescriptor cur)
			&& next.provider() instanceof ProcessDescriptor nex) {

			// prefer library processes
			if (cur.isFromLibrary() && !nex.isFromLibrary())
				return false;
			if (nex.isFromLibrary() && !cur.isFromLibrary())
				return true;

			// prefer LCI results to unit processes
			if (cur.processType == ProcessType.LCI_RESULT
				&& nex.processType != ProcessType.LCI_RESULT)
				return false;
			if (nex.processType == ProcessType.LCI_RESULT
				&& cur.processType != ProcessType.LCI_RESULT)
				return true;
		}

		// cannot really make a decision
		return false;
	}
}
