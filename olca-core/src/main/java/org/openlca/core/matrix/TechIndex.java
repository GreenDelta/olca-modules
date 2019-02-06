package org.openlca.core.matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
	private final HashMap<ProcessProduct, Integer> index = new HashMap<>();

	/**
	 * Contains the product-outputs and waste-inputs in an ordinal list.
	 */
	private final ArrayList<ProcessProduct> providers = new ArrayList<>();

	/**
	 * Maps linked exchanges (keys) as (processId, exchangeId) pairs to the
	 * respective provider.
	 */
	private final HashMap<LongPair, ProcessProduct> links = new HashMap<>();

	/**
	 * Maps the process IDs to the list of product-outputs and waste-inputs
	 * provided by the respective process.
	 */
	private final HashMap<Long, List<ProcessProduct>> processProviders = new HashMap<>();

	/**
	 * The demand value of the reference flow of the product system described by
	 * this index. The value is given in the reference unit and quantity of the
	 * respective flow.
	 */
	private double demand = 1d;

	/**
	 * Creates a new technosphere index of a product system.
	 *
	 * @param refFlow the reference product-output or waste-input as (processId,
	 *                flowId) pair.
	 */
	public TechIndex(ProcessProduct refFlow) {
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
	public ProcessProduct getRefFlow() {
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
	public int getIndex(ProcessProduct provider) {
		Integer idx = index.get(provider);
		if (idx == null)
			return -1;
		return idx;
	}

	/**
	 * Returns true if the given provider (product-output or waste-input) is
	 * contained in this index.
	 */
	public boolean contains(ProcessProduct provider) {
		return index.containsKey(provider);
	}

	/**
	 * Returns true when there is a product in this index with a process and
	 * flow of the given IDs.
	 */
	public boolean contains(long processID, long flowID) {
		return getProvider(processID, flowID) != null;
	}

	public void each(IndexConsumer<ProcessProduct> fn) {
		for (int i = 0; i < providers.size(); i++) {
			fn.accept(i, providers.get(i));
		}
	}

	public ProcessProduct getProvider(long processID, long flowID) {
		List<ProcessProduct> list = processProviders.get(processID);
		if (list == null)
			return null;
		for (ProcessProduct p : list) {
			if (p.flowId() == flowID)
				return p;
		}
		return null;
	}

	/**
	 * Adds the given provider (product-output or waste-input) to this index.
	 * Does nothing if it is already contained in this index.
	 */
	public void put(ProcessProduct provider) {
		if (contains(provider))
			return;
		int idx = index.size();
		index.put(provider, idx);
		List<ProcessProduct> list = processProviders.get(provider.id());
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
	public ProcessProduct getProviderAt(int index) {
		return providers.get(index);
	}

	/**
	 * Get all providers with the given descriptor of a process or product
	 * system as entity.
	 */
	public List<ProcessProduct> getProviders(CategorizedDescriptor d) {
		if (d == null)
			return Collections.emptyList();
		List<ProcessProduct> providers = processProviders.get(d.id);
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
	public List<ProcessProduct> getProviders(long processId) {
		List<ProcessProduct> providers = processProviders.get(processId);
		if (providers == null)
			return Collections.emptyList();
		return new ArrayList<>(providers);
	}

	/**
	 * Adds a process link to this index.
	 *
	 * @param exchange The linked product-input or waste-output as (processId,
	 *                 exchangeId) pair.
	 * @param provider The product-output or waste-input (provider) as
	 *                 (processId, flowId) pair.
	 */
	public void putLink(LongPair exchange, ProcessProduct provider) {
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
	public ProcessProduct getLinkedProvider(LongPair exchange) {
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
	 * Returns the IDs of all processes in this index (note that this can also
	 * can contain product system IDs if there are sub-systems in the product
	 * system).
	 */
	public Set<Long> getProcessIds() {
		HashSet<Long> set = new HashSet<>();
		for (ProcessProduct p : providers) {
			set.add(p.id());
		}
		return set;
	}

	/**
	 * Returns all providers of this index.
	 */
	public Set<ProcessProduct> content() {
		return new HashSet<>(providers);
	}
}
