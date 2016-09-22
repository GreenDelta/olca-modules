package org.openlca.core.matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The TechIndex maps the linked product-outputs and waste-inputs (provider-flow
 * pairs) of a product system to a matrix index that is used in the calculation
 * and results. It also contains the process links of that product system.
 */
public class TechIndex {

	/**
	 * Maps the product-outputs and waste-inputs as (processId, flowId) pairs to
	 * an ordinal index.
	 */
	private final HashMap<LongPair, Integer> index = new HashMap<>();

	/**
	 * Contains the product-outputs and waste-inputs in an ordinal list.
	 */
	private final ArrayList<LongPair> providers = new ArrayList<>();

	/**
	 * Maps linked exchanges (keys) as (processId, exchangeId) pairs to the
	 * respective provider.
	 */
	private final HashMap<LongPair, LongPair> links = new HashMap<>();

	/**
	 * Maps the process IDs to the list of product-outputs and waste-inputs
	 * provided by the respective process.
	 */
	private final HashMap<Long, List<LongPair>> processProviders = new HashMap<>();

	/**
	 * The demand value of the reference flow of the product system described by
	 * this index. The value is given in the reference unit and quantity of the
	 * respective flow.
	 */
	private double demand = 1d;

	/**
	 * Creates a new technosphere index of a product system.
	 * 
	 * @param refFlow
	 *            the reference product-output or waste-input as (processId,
	 *            flowId) pair.
	 */
	public TechIndex(LongPair refFlow) {
		put(refFlow);
	}

	/**
	 * The demand value. This is the amount of the reference flow given in the
	 * reference unit and flow property. The default value is 1.0.
	 */
	public void setDemand(double demand) {
		this.demand = demand;
	}

	/**
	 * Get the reference product-output or waste-input of the product system
	 * described by this index.
	 */
	public LongPair getRefFlow() {
		return providers.get(0);
	}

	/**
	 * The demand value. This is the amount of the reference flow given in the
	 * reference unit and flow property. The default value is 1.0.
	 */
	public double getDemand() {
		return demand;
	}

	/**
	 * Returns the size of this index which is equal to the number of rows and
	 * columns in the related technology matrix.
	 */
	public int size() {
		return index.size();
	}

	/**
	 * Returns the ordinal index of the given provider (product-output or waste
	 * input).
	 */
	public int getIndex(LongPair provider) {
		Integer idx = index.get(provider);
		if (idx == null)
			return -1;
		return idx;
	}

	/**
	 * Returns true if the given provider (product-output or waste-input) is
	 * contained in this index.
	 */
	public boolean contains(LongPair provider) {
		return index.containsKey(provider);
	}

	/**
	 * Adds the given provider (product-output or waste-input) to this index.
	 * Does nothing if it is already contained in this index.
	 */
	public void put(LongPair provider) {
		if (contains(provider))
			return;
		int idx = index.size();
		index.put(provider, idx);
		long processId = provider.getFirst();
		List<LongPair> list = processProviders.get(processId);
		if (list == null) {
			list = new ArrayList<>();
			processProviders.put(processId, list);
		}
		list.add(provider);
		providers.add(provider);
	}

	/**
	 * Returns the provider (product-output or waste-input) at the given index.
	 */
	public LongPair getProviderAt(int index) {
		return providers.get(index);
	}

	/**
	 * Returns the providers (product-outputs and waste-inputs) for the process
	 * with the given ID.
	 */
	public List<LongPair> getProviders(long processId) {
		List<LongPair> providers = processProviders.get(processId);
		if (providers == null)
			return Collections.emptyList();
		return new ArrayList<>(providers);
	}

	/**
	 * Adds a process link to this index.
	 * 
	 * @param exchange
	 *            The linked product-input or waste-output as (processId,
	 *            exchangeId) pair.
	 * @param provider
	 *            The product-output or waste-input (provider) as (processId,
	 *            flowId) pair.
	 */
	public void putLink(LongPair exchange, LongPair provider) {
		if (links.containsKey(exchange))
			return;
		put(provider);
		links.put(exchange, provider);
	}

	/**
	 * 
	 * Returns true if the given product-input or waste-output is linked to a
	 * provider of this index.
	 */
	public boolean isLinked(LongPair exchange) {
		return links.containsKey(exchange);
	}

	/**
	 * Returns the linked provider (product-output or waste-input) for the given
	 * exchange (product-input or waste-output)
	 */
	public LongPair getLinkedProvider(LongPair exchange) {
		return links.get(exchange);
	}

	/**
	 * Returns all exchanges (product-inputs and waste-outputs) that are linked
	 * to provider of this index.
	 */
	public Set<LongPair> getLinkedExchanges() {
		return links.keySet();
	}

	/**
	 * Returns the IDs of all processes in this index.
	 */
	public Set<Long> getProcessIds() {
		HashSet<Long> set = new HashSet<>();
		for (LongPair product : providers) {
			set.add(product.getFirst());
		}
		return set;
	}
}
