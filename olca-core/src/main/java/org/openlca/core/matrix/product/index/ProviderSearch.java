package org.openlca.core.matrix.product.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.LinkingConfig;
import org.openlca.core.matrix.LinkingConfig.DefaultProviders;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.cache.ProcessTable;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProcessType;

/**
 * Searches for the best provider for a given product input or waste output in
 * the database.
 */
public class ProviderSearch {

	private final ProcessTable processTable;
	private final LinkingConfig config;

	public ProviderSearch(ProcessTable processTable, LinkingConfig config) {
		this.processTable = processTable;
		this.config = config;
	}

	/**
	 * Find the best provider for the given product input or waste output
	 * according to the search settings.
	 */
	public LongPair find(CalcExchange e) {
		if (e == null || cancel())
			return null;
		long productId = e.flowId;
		long[] processIds = processTable.getProviders(productId);
		if (processIds == null || processIds.length == 0)
			return null;

		// select a default provider if present
		// this needs to be done before asking a potential callback
		// for options as the callback should be only called when
		// there are multiple options.
		if (config.providerLinking != DefaultProviders.IGNORE) {
			for (long processId : processIds) {
				if (processId == e.defaultProviderId)
					return LongPair.of(processId, productId);
			}
			if (config.providerLinking == DefaultProviders.ONLY)
				return null;
		}

		// check form single options and callback
		if (processIds.length == 1)
			return LongPair.of(processIds[0], productId);
		if (config.callback != null) {
			processIds = config.callback.select(e, processIds);
			if (processIds == null || processIds.length == 0)
				return null;
			if (processIds.length == 1)
				return LongPair.of(processIds[0], productId);
		}

		LongPair candidate = null;
		for (long processId : processIds) {
			LongPair newOption = LongPair.of(processId, productId);
			if (isBetter(e, candidate, newOption)) {
				candidate = newOption;
			}
		}
		return candidate;
	}

	private boolean isBetter(CalcExchange e, LongPair old, LongPair newOption) {
		if (old == null)
			return true;
		if (newOption == null)
			return false;
		if (config.providerLinking != DefaultProviders.IGNORE) {
			if (old.getFirst() == e.defaultProviderId)
				return false;
			if (newOption.getFirst() == e.defaultProviderId)
				return true;
		}
		ProcessType oldType = processTable.getType(old.getFirst());
		ProcessType newType = processTable.getType(newOption.getFirst());
		if (oldType == config.preferredType
				&& newType != config.preferredType)
			return false;
		return oldType != config.preferredType
				&& newType == config.preferredType;
	}

	/**
	 * Returns from the given list the product inputs or waste outputs that
	 * could be linked to a provider.
	 */
	public List<CalcExchange> getLinkCandidates(List<CalcExchange> list) {
		if (list == null || list.isEmpty() || cancel())
			return Collections.emptyList();
		List<CalcExchange> candidates = new ArrayList<>();
		for (CalcExchange e : list) {
			if (config.providerLinking == DefaultProviders.ONLY
					&& e.defaultProviderId == 0L)
				continue;
			if (e.flowType == null || e.flowType == FlowType.ELEMENTARY_FLOW)
				continue;
			if (e.isInput && e.flowType == FlowType.PRODUCT_FLOW) {
				candidates.add(e);
			} else if (!e.isInput && e.flowType == FlowType.WASTE_FLOW) {
				candidates.add(e);
			}
		}
		return candidates;
	}

	private boolean cancel() {
		return config.callback != null
				&& config.callback.cancel();
	}

}
