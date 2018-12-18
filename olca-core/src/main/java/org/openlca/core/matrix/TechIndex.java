package org.openlca.core.matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.model.descriptors.CategorizedDescriptor;

/**
 * The index $\mathit{Idx}_A$ of the technology matrix $\mathbf{A}$ of a product
 * system. It maps the process-product pairs (or process-waste pairs)
 * $\mathit{P}$ of the product system to the respective $n$ rows and columns of
 * $\mathbf{A}$. If the product system contains other product systems as
 * sub-systems, these systems are handled like processes and are also mapped as
 * pair with their quantitative reference flow to that index (and also their
 * processes etc.).
 * 
 * $$\mathit{Idx}_A: \mathit{P} \mapsto [0 \dots n-1]$$
 */
public class TechIndex {

	/**
	 * Maps the product-outputs and waste-inputs as (processId, flowId) pairs to
	 * an ordinal index.
	 */
	private final HashMap<Provider, Integer> index = new HashMap<>();

	/**
	 * Contains the product-outputs and waste-inputs in an ordinal list.
	 */
	private final ArrayList<Provider> providers = new ArrayList<>();

	/**
	 * Maps linked exchanges (keys) as (processId, exchangeId) pairs to the
	 * respective provider.
	 */
	private final HashMap<LongPair, Provider> links = new HashMap<>();

	/**
	 * Maps the process IDs to the list of product-outputs and waste-inputs
	 * provided by the respective process.
	 */
	private final HashMap<Long, List<Provider>> processProviders = new HashMap<>();

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
	public TechIndex(Provider refFlow) {
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
	public Provider getRefFlow() {
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
	public int getIndex(Provider provider) {
		Integer idx = index.get(provider);
		if (idx == null)
			return -1;
		return idx;
	}

	/**
	 * Returns true if the given provider (product-output or waste-input) is
	 * contained in this index.
	 */
	public boolean contains(Provider provider) {
		return index.containsKey(provider);
	}

	public boolean contains(long id, long flowId) {
		return getProvider(id, flowId) != null;
	}

	public Provider getProvider(long id, long flowId) {
		List<Provider> list = processProviders.get(id);
		if (list == null)
			return null;
		for (Provider p : list) {
			if (p.flowId() == flowId)
				return p;
		}
		return null;
	}

	/**
	 * Adds the given provider (product-output or waste-input) to this index.
	 * Does nothing if it is already contained in this index.
	 */
	public void put(Provider provider) {
		if (contains(provider))
			return;
		int idx = index.size();
		index.put(provider, idx);
		List<Provider> list = processProviders.get(provider.id());
		if (list == null) {
			list = new ArrayList<>();
			processProviders.put(provider.id(), list);
		}
		list.add(provider);
		providers.add(provider);
	}

	/**
	 * Returns the provider (product-output or waste-input) at the given index.
	 */
	public Provider getProviderAt(int index) {
		return providers.get(index);
	}

	/**
	 * Get all providers with the given descriptor of a process or product
	 * system as entity.
	 */
	public List<Provider> getProviders(CategorizedDescriptor d) {
		if (d == null)
			return Collections.emptyList();
		List<Provider> providers = processProviders.get(d.getId());
		if (providers == null)
			return Collections.emptyList();
		return new ArrayList<>(providers);
	}

	/**
	 * Returns the providers (product-outputs and waste-inputs) for the process
	 * with the given ID.
	 *
	 * TODO: do we need this anymore?
	 */
	public List<Provider> getProviders(long processId) {
		List<Provider> providers = processProviders.get(processId);
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
	public void putLink(LongPair exchange, Provider provider) {
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
	public Provider getLinkedProvider(LongPair exchange) {
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
	 *
	 * TODO: this can be now a set of process AND product system IDs.
	 */
	@Deprecated
	public Set<Long> getProcessIds() {
		HashSet<Long> set = new HashSet<>();
		for (Provider p : providers) {
			set.add(p.id());
		}
		return set;
	}

	/**
	 * Returns all providers of this index.
	 */
	public Set<Provider> content() {
		return new HashSet<>(providers);
	}
}
