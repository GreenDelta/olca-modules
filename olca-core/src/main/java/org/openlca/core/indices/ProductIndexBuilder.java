package org.openlca.core.indices;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.lean.TechnosphereLink;

// TODO: cut-offs
public class ProductIndexBuilder {

	private ProcessType preferredType = ProcessType.LCI_RESULT;
	private IDatabase database;
	private LongPair refProduct;
	private TechnosphereLinkIndex linkIndex;
	private ProcessTypeIndex typeIndex;

	public ProductIndexBuilder(IDatabase database, LongPair refProduct) {
		this.database = database;
		this.refProduct = refProduct;
	}

	public void setPreferredType(ProcessType preferredType) {
		this.preferredType = preferredType;
	}

	public ProductIndex build() {
		linkIndex = new TechnosphereLinkIndex(database);
		typeIndex = new ProcessTypeIndex(database);
		ProductIndex index = new ProductIndex();
		Queue<LongPair> queue = new ArrayDeque<>();
		List<LongPair> handled = new ArrayList<>();
		queue.add(refProduct);
		index.put(refProduct);
		while (!queue.isEmpty()) {
			LongPair recipient = queue.poll();
			handled.add(recipient);
			List<TechnosphereLink> inputLinks = linkIndex
					.getProductInputs(recipient.getFirst());
			for (TechnosphereLink inputLink : inputLinks) {
				LongPair provider = findProvider(inputLink);
				if (provider == null)
					continue;
				index.putLink(recipient, provider);
				if (!handled.contains(provider) && !queue.contains(provider))
					queue.add(provider);
			}
		}
		return index;
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
