package org.openlca.core.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps output products of processes to an index 0 <= i < n. For products that
 * are not contained in the index i is -1.
 */
public class ProductIndex {

	private Logger log = LoggerFactory.getLogger(getClass());

	/** exchange ID of output product -> index */
	private Map<Long, Integer> productIndex = new HashMap<>();

	private Map<Long, Long> inputOutputLinks = new HashMap<>();

	/** process-Id -> exchange IDs of output products */
	private Map<Long, List<Long>> processProducts = new HashMap<>();

	private List<Process> processes = new ArrayList<>();
	private List<Exchange> exchanges = new ArrayList<>();

	public ProductIndex(ProductSystem system) {
		create(system);
	}

	private void create(ProductSystem system) {
		log.trace("create product index");
		indexProduct(system.getReferenceProcess(),
				system.getReferenceExchange());
		for (ProcessLink link : system.getProcessLinks()) {
			indexProduct(link.getProviderProcess(), link.getProviderOutput());
			inputOutputLinks.put(link.getRecipientInput().getId(), link
					.getProviderOutput().getId());
		}
		log.trace("product index with {} entries created", size());
	}

	public int size() {
		return productIndex.size();
	}

	public int getIndex(Long productKey) {
		Integer idx = productIndex.get(productKey);
		if (idx == null)
			return -1;
		return idx;
	}

	public int getIndex(Process process, Exchange product) {
		return getIndex(product.getId());
	}

	public boolean contains(Process process, Exchange product) {
		return productIndex.containsKey(product.getId());
	}

	private void indexProduct(Process process, Exchange exchange) {
		Long key = exchange.getId();
		Integer idx = productIndex.get(key);
		if (idx != null)
			return; // already indexed
		idx = productIndex.size();
		productIndex.put(key, idx);
		List<Long> products = processProducts.get(process.getId());
		if (products == null) {
			products = new ArrayList<>();
			processProducts.put(process.getId(), products);
		}
		products.add(key);
		processes.add(process);
		exchanges.add(exchange);
	}

	/** Get the exchange IDs of the output products of the given process. */
	public List<Long> getProducts(Process process) {
		List<Long> products = processProducts.get(process.getId());
		if (products == null)
			return Collections.emptyList();
		return products;
	}

	public Process getProcessAt(int index) {
		return processes.get(index);
	}

	public Exchange getProductAt(int index) {
		return exchanges.get(index);
	}

	public boolean isLinkedInput(Exchange exchange) {
		return inputOutputLinks.containsKey(exchange.getId());
	}

	public Long getLinkedOutputKey(Exchange input) {
		return inputOutputLinks.get(input.getId());
	}

	/**
	 * Returns the list of the IDs of all products in this index. These are the
	 * IDs of the output exchanges which are products.
	 */
	public List<Long> getProductIds() {
		List<Long> ids = new ArrayList<>(size() + 2);
		ids.addAll(productIndex.keySet());
		return ids;
	}

}
