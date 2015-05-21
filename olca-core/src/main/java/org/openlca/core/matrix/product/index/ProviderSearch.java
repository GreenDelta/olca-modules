package org.openlca.core.matrix.product.index;

import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.cache.ProcessTable;
import org.openlca.core.model.ProcessType;

/**
 * Searches for the best provider for a given input product in the database.
 */
class ProviderSearch {

	private ProcessTable processTable;
	private ProcessType preferredType;
	
	public ProviderSearch(ProcessTable processTable, ProcessType preferredType) {
		this.processTable = processTable;
		this.preferredType = preferredType;
	}
	
	void setPreferredType(ProcessType preferredType) {
		this.preferredType = preferredType;
	}
	
	LongPair find(CalcExchange productInput) {
		if (productInput == null)
			return null;
		long productId = productInput.getFlowId();
		long[] processIds = processTable.getProductProviders(productId);
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
		if (candidate.getFirst() == inputLink.getDefaultProviderId())
			return false;
		if (newOption.getFirst() == inputLink.getDefaultProviderId())
			return true;
		ProcessType candidateType = processTable.getType(candidate.getFirst());
		ProcessType newOptionType = processTable.getType(newOption.getFirst());
		if (candidateType == preferredType && newOptionType != preferredType)
			return false;
        return candidateType != preferredType && newOptionType == preferredType;
    }
	
}
