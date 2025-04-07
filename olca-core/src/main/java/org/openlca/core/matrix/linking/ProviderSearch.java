package org.openlca.core.matrix.linking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.cache.ProviderMap;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.linking.LinkingConfig.PreferredType;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * Searches for the best provider for a given product input or waste output in
 * the database.
 */
public record ProviderSearch(ProviderMap providerMap, LinkingConfig config) {

	/**
	 * Find the best provider for the given product input or waste output
	 * according to the search settings.
	 */
	public TechFlow find(CalcExchange e) {
		if (e == null || cancel())
			return null;
		List<TechFlow> providers = providerMap.getProvidersOf(e.flowId);
		if (providers.isEmpty())
			return null;

		// select a default provider if present this needs to be done before asking
		// a potential callback for options as the callback should be only called
		// when there are multiple options.
		if (config.providerLinking() != ProviderLinking.IGNORE_DEFAULTS) {
			for (TechFlow provider : providers) {
				if (provider.providerId() == e.defaultProviderId)
					return provider;
			}
			if (config.providerLinking() == ProviderLinking.ONLY_DEFAULTS)
				return null;
		}

		// check form single options and callback
		if (providers.size() == 1)
			return providers.getFirst();
		if (config.callback() != null) {
			providers = config.callback().select(e, providers);
			if (providers == null || providers.isEmpty())
				return null;
			if (providers.size() == 1)
				return providers.getFirst();
		}

		TechFlow candidate = null;
		for (TechFlow next : providers) {
			if (isBetter(e, candidate, next)) {
				candidate = next;
			}
		}
		return candidate;
	}

	private boolean isBetter(CalcExchange e, TechFlow old, TechFlow newOption) {
		if (old == null)
			return true;
		if (newOption == null)
			return false;
		if (config.providerLinking() != ProviderLinking.IGNORE_DEFAULTS) {
			if (old.providerId() == e.defaultProviderId)
				return false;
			if (newOption.providerId() == e.defaultProviderId)
				return true;
		}
		var oldType = typeOf(old.providerId());
		var newType = typeOf(newOption.providerId());
		if (oldType == config.preferredType()
				&& newType != config.preferredType())
			return false;
		return oldType != config.preferredType()
			&& newType == config.preferredType();
	}

	private PreferredType typeOf(long providerId) {
		var provider = providerMap.getProvider(providerId);
		if (provider == null)
			return null;
		if (provider instanceof ProcessDescriptor p)
			return  p.processType == ProcessType.UNIT_PROCESS
					? PreferredType.UNIT_PROCESS
					: PreferredType.SYSTEM_PROCESS;
		return provider.type == ModelType.RESULT
				? PreferredType.RESULT
				: null;
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
			if (config.providerLinking() == ProviderLinking.ONLY_DEFAULTS
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
		return config.callback() != null
				&& config.callback().cancel();
	}

	TechFlow getProvider(long id, long flowId) {
		return providerMap.getTechFlow(id, flowId);
	}

}
