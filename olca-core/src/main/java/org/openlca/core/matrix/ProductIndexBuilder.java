package org.openlca.core.matrix;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ProcessType;

// TODO: cut-offs
public class ProductIndexBuilder {

	private ProcessType preferredType = ProcessType.LCI_RESULT;
	private IDatabase database;
	private TechnosphereLinkIndex linkIndex;
	private ProcessTypeIndex typeIndex;

	public ProductIndexBuilder(IDatabase database) {
		this.database = database;
	}

	public void setPreferredType(ProcessType preferredType) {
		this.preferredType = preferredType;
	}

	public ProductIndex build(LongPair refProduct) {
		return build(refProduct, 1.0);
	}

	public ProductIndex build(LongPair refProduct, double demand) {
		linkIndex = new TechnosphereLinkIndex(database);
		typeIndex = ProcessTypeIndex.create(database);
		ProductIndex index = new ProductIndex(refProduct, demand);
		Queue<LongPair> queue = new ArrayDeque<>();
		List<LongPair> handled = new ArrayList<>();
		queue.add(refProduct);
		while (!queue.isEmpty()) {
			LongPair recipient = queue.poll();
			indexAllocation(recipient, index);
			handled.add(recipient);
			List<TechnosphereLink> inputLinks = linkIndex
					.getProductInputs(recipient.getFirst());
			for (TechnosphereLink inputLink : inputLinks) {
				LongPair provider = findProvider(inputLink);
				if (provider == null)
					continue;
				LongPair recipientInput = new LongPair(
						inputLink.getProcessId(), inputLink.getFlowId());
				index.putLink(recipientInput, provider);
				if (!handled.contains(provider) && !queue.contains(provider))
					queue.add(provider);
			}
		}
		return index;
	}

	private void indexAllocation(LongPair recipient, ProductIndex index) {
		long processId = recipient.getFirst();
		AllocationMethod method = typeIndex
				.getDefaultAllocationMethod(processId);
		index.putDefaultAllocationMethod(processId, method);
	}

	private LongPair findProvider(TechnosphereLink inputLink) {
		TechnosphereLink candidate = null;
		List<TechnosphereLink> outputLinks = linkIndex
				.getProductOutputs(inputLink.getFlowId());
		for (TechnosphereLink outputLink : outputLinks) {
			if (isBetter(inputLink, candidate, outputLink))
				candidate = outputLink;
		}
		if (candidate == null)
			return null;
		return new LongPair(candidate.getProcessId(), candidate.getFlowId());
	}

	private boolean isBetter(TechnosphereLink inputLink,
			TechnosphereLink candidate, TechnosphereLink newOption) {
		if (candidate == null)
			return true;
		if (newOption == null)
			return false;
		if (candidate.getProcessId() == inputLink.getDefaultProviderId())
			return false;
		if (newOption.getProcessId() == inputLink.getDefaultProviderId())
			return true;
		ProcessType candidateType = typeIndex.getType(candidate.getProcessId());
		ProcessType newOptionType = typeIndex.getType(newOption.getProcessId());
		if (candidateType == preferredType && newOptionType != preferredType)
			return false;
		if (candidateType != preferredType && newOptionType == preferredType)
			return true;
		return newOption.getAmount() > candidate.getAmount();
	}
}
