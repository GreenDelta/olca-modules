package org.openlca.core.matrix.product.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.cache.ProcessTable;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProcessType;

/**
 * Searches for the best provider for a given product input or waste output in
 * the database.
 */
public class ProviderSearch {

	private ProcessTable processTable;
	private ProcessType preferredType;
	private LinkingMethod linkingMethod;

	public ProviderSearch(ProcessTable processTable) {
		this.processTable = processTable;
		this.preferredType = ProcessType.LCI_RESULT;
		this.linkingMethod = LinkingMethod.ONLY_LINK_PROVIDERS;
	}

	public void setPreferredType(ProcessType preferredType) {
		if (preferredType == null)
			return;
		this.preferredType = preferredType;
	}

	public void setLinkingMethod(LinkingMethod linkingMethod) {
		if (linkingMethod == null)
			return;
		this.linkingMethod = linkingMethod;
	}

	public LongPair find(CalcExchange productInput) {
		if (productInput == null)
			return null;
		long productId = productInput.flowId;
		long[] processIds = processTable.getProviders(productId);
		if (processIds == null)
			return null;
		LongPair candidate = null;
		for (long processId : processIds) {
			LongPair newOption = LongPair.of(processId, productId);
			if (isBetter(productInput, candidate, newOption))
				candidate = newOption;
		}
		return candidate;
	}

	private boolean isBetter(CalcExchange inputLink, LongPair candidate,
			LongPair newOption) {
		if (candidate == null)
			return true;
		if (newOption == null)
			return false;
		if (linkingMethod != LinkingMethod.IGNORE_PROVIDERS) {
			if (candidate.getFirst() == inputLink.defaultProviderId)
				return false;
			if (newOption.getFirst() == inputLink.defaultProviderId)
				return true;
		}
		ProcessType candidateType = processTable.getType(candidate.getFirst());
		ProcessType newOptionType = processTable.getType(newOption.getFirst());
		if (candidateType == preferredType && newOptionType != preferredType)
			return false;
		return candidateType != preferredType && newOptionType == preferredType;
	}

	/**
	 * Returns from the given list the product inputs or waste outputs that
	 * could be linked to a provider.
	 */
	public List<CalcExchange> getLinkCandidates(List<CalcExchange> list) {
		if (list == null || list.isEmpty())
			return Collections.emptyList();
		List<CalcExchange> candidates = new ArrayList<>();
		for (CalcExchange e : list) {
			if (linkingMethod == LinkingMethod.ONLY_LINK_PROVIDERS && e.defaultProviderId == 0l)
				continue;
			if (e.flowType == null || e.flowType == FlowType.ELEMENTARY_FLOW)
				continue;
			if (e.isInput && e.flowType == FlowType.PRODUCT_FLOW)
				candidates.add(e);
			else if (!e.isInput && e.flowType == FlowType.WASTE_FLOW)
				candidates.add(e);
		}
		return candidates;
	}

}
