package org.openlca.core.matrix.product.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.cache.ProcessTable;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.ProductSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductIndexBuilder implements IProductIndexBuilder {

	private Logger log = LoggerFactory.getLogger(getClass());
	private ProcessType preferredType = ProcessType.LCI_RESULT;
	private MatrixCache cache;
	private ProcessTable processTable;
	private ProductSystem system;

	public ProductIndexBuilder(MatrixCache cache, ProductSystem system) {
		this.cache = cache;
		this.system = system;
		this.processTable = cache.getProcessTable();
	}

	@Override
	public void setPreferredType(ProcessType preferredType) {
		this.preferredType = preferredType;
	}

	@Override
	public ProductIndex build(LongPair refProduct) {
		return build(refProduct, 1.0);
	}

	@Override
	public ProductIndex build(LongPair refProduct, double demand) {
		log.trace("build product index for {}", refProduct);
		ProductIndex index = new ProductIndex(refProduct);
		if (system != null) {
			for (ProcessLink link : system.getProcessLinks()) {
				LongPair inputKey = new LongPair(link.recipientId, link.flowId);
				LongPair outputKey = new LongPair(link.providerId, link.flowId);
				index.putLink(inputKey, outputKey);
			}
		}
		index.setDemand(demand);
		List<LongPair> block = new ArrayList<>();
		block.add(refProduct);
		HashSet<LongPair> handled = new HashSet<>();
		while (!block.isEmpty()) {
			List<LongPair> nextBlock = new ArrayList<>();
			log.trace("fetch next block with {} entries", block.size());
			Map<Long, List<CalcExchange>> exchanges = fetchExchanges(block);
			for (LongPair recipient : block) {
				handled.add(recipient);
				List<CalcExchange> processExchanges = exchanges.get(recipient
						.getFirst());
				List<CalcExchange> productInputs = getProductInputs(
						processExchanges);
				for (CalcExchange productInput : productInputs) {
					LongPair provider = findProvider(productInput);
					if (provider == null)
						continue;
					LongPair recipientInput = new LongPair(
							recipient.getFirst(), productInput.flowId);
					index.putLink(recipientInput, provider);
					if (!handled.contains(provider)
							&& !nextBlock.contains(provider))
						nextBlock.add(provider);
				}
			}
			block = nextBlock;
		}
		return index;
	}

	private List<CalcExchange> getProductInputs(
			List<CalcExchange> processExchanges) {
		if (processExchanges == null || processExchanges.isEmpty())
			return Collections.emptyList();
		List<CalcExchange> productInputs = new ArrayList<>();
		for (CalcExchange exchange : processExchanges) {
			if (!exchange.input)
				continue;
			if (exchange.flowType == FlowType.ELEMENTARY_FLOW)
				continue;
			productInputs.add(exchange);
		}
		return productInputs;
	}

	private Map<Long, List<CalcExchange>> fetchExchanges(List<LongPair> block) {
		if (block.isEmpty())
			return Collections.emptyMap();
		Set<Long> processIds = new HashSet<>();
		for (LongPair pair : block)
			processIds.add(pair.getFirst());
		try {
			return cache.getExchangeCache().getAll(processIds);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to load exchanges from cache", e);
			return Collections.emptyMap();
		}
	}

	private LongPair findProvider(CalcExchange productInput) {
		if (productInput == null)
			return null;
		long productId = productInput.flowId;
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
		if (candidate.getFirst() == inputLink.defaultProviderId)
			return false;
		if (newOption.getFirst() == inputLink.defaultProviderId)
			return true;
		ProcessType candidateType = processTable.getType(candidate.getFirst());
		ProcessType newOptionType = processTable.getType(newOption.getFirst());
		if (candidateType == preferredType && newOptionType != preferredType)
			return false;
		return candidateType != preferredType && newOptionType == preferredType;
	}
}
