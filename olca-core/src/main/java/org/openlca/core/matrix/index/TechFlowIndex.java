package org.openlca.core.matrix.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.math.ReferenceAmount;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.TechLinker;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * The index $\mathit{Idx}_A$ of the technology matrix $\mathbf{A}$ of a product
 * system. It maps the process-product pairs (or process-waste pairs)
 * $\mathit{P}$ of the product system to the respective $n$ rows and columns of
 * $\mathbf{A}$. If the product system contains other product systems as
 * sub-systems, these systems are handled like processes and are also mapped as
 * pair with their quantitative reference flow to that index (and also their
 * processes etc.).
 * <p>
 * $$\mathit{Idx}_A: \mathit{P} \mapsto [0 \dots n-1]$$
 */
public final class TechFlowIndex implements TechLinker, MatrixIndex<TechFlow> {

	/**
	 * Maps the product-outputs and waste-inputs as (processId, flowId) pairs to an
	 * ordinal index.
	 */
	private final HashMap<TechFlow, Integer> index = new HashMap<>();

	/**
	 * Contains the product-outputs and waste-inputs in an ordinal list.
	 */
	private final ArrayList<TechFlow> providers = new ArrayList<>();

	/**
	 * Maps linked exchanges (keys) as (processId, exchangeId) pairs to the
	 * respective provider.
	 */
	private final HashMap<LongPair, TechFlow> links = new HashMap<>();

	/**
	 * Maps the IDs of the processes and product systems to the list of
	 * product-outputs and waste-inputs provided by these respectively.
	 */
	private final TLongObjectHashMap<List<TechFlow>> processProviders = new TLongObjectHashMap<>();

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
	public TechFlowIndex(TechFlow refFlow) {
		add(refFlow);
	}

	/**
	 * Creates the index for the given product system. If the `withoutNetwork`
	 * attribute of the system is set to true, it creates an unlinked index with all
	 * process products (and waste flows) from the database. Otherwise all process
	 * links of the product system are directly stored in the index.
	 */
	public static TechFlowIndex of(ProductSystem system, IDatabase db) {
		var index = initFrom(system);
		if (system.withoutNetwork) {
			eachProviderOf(db, index::add);
			return index;
		}

		// initialize the fast descriptor maps
		var systems = new ProductSystemDao(db).descriptorMap();
		var processes = new ProcessDao(db).descriptorMap();
		var flows = new FlowDao(db).descriptorMap();

		for (var link : system.processLinks) {
			CategorizedDescriptor p = processes.get(link.providerId);
			if (p == null) {
				p = systems.get(link.providerId);
				if (p == null)
					continue;
			}
			var flow = flows.get(link.flowId);
			if (flow == null)
				continue;

			// the tech-index checks for duplicates of products and links
			var provider = TechFlow.of(p, flow);
			index.add(provider);
			var exchange = new LongPair(link.processId, link.exchangeId);
			index.putLink(exchange, provider);
		}
		return index;
	}

	/**
	 * Creates an unlinked index of all process products (and waste flows) of the
	 * database in some arbitrary order.
	 */
	public static TechFlowIndex of(IDatabase db) {
		var list = new ArrayList<TechFlow>();
		eachProviderOf(db, list::add);
		if (list.isEmpty())
			throw new RuntimeException("no providers in database");
		var index = new TechFlowIndex(list.get(0));
		for (int i = 1; i < list.size(); i++) {
			index.add(list.get(i));
		}
		return index;
	}

	private static TechFlowIndex initFrom(ProductSystem system) {
		// initialize the TechIndex with the reference flow
		var refExchange = system.referenceExchange;
		var refFlow = TechFlow.of(
				system.referenceProcess, refExchange.flow);
		var index = new TechFlowIndex(refFlow);

		// set the final demand value which is negative
		// when we have a waste flow as reference flow
		double demand = ReferenceAmount.get(system);
		var ftype = refExchange.flow == null
				? null
				: refExchange.flow.flowType;
		if (ftype == FlowType.WASTE_FLOW) {
			demand = -demand;
		}
		index.setDemand(demand);
		return index;
	}

	private static void eachProviderOf(IDatabase db, Consumer<TechFlow> fn) {
		var processes = new ProcessDao(db).descriptorMap();
		var flows = new FlowDao(db).descriptorMap();
		String sql = "select f_owner, f_flow, is_input from tbl_exchanges";
		NativeSql.on(db).query(sql, r -> {
			long flowID = r.getLong(2);
			var flow = flows.get(flowID);
			if (flow == null
					|| flow.flowType == null
					|| flow.flowType == FlowType.ELEMENTARY_FLOW)
				return true;
			var type = flow.flowType;
			boolean isInput = r.getBoolean(3);
			if (isInput && type == FlowType.PRODUCT_FLOW)
				return true;
			if (!isInput && type == FlowType.WASTE_FLOW)
				return true;
			long procID = r.getLong(1);
			var process = processes.get(procID);
			if (process == null) {
				// note that product system results could be
				// stored in the exchanges table; in this
				// case the process would be null.
				return true;
			}
			fn.accept(TechFlow.of(process, flow));
			return true;
		});
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
	public TechFlow getRefFlow() {
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
	@Override
	public int size() {
		return index.size();
	}

	/**
	 * Returns the ordinal index of the given product (product-output or waste
	 * input). If the product is not not part of this index, -1 is returned.
	 */
	@Override
	public int of(TechFlow provider) {
		var idx = index.get(provider);
		return idx == null ? -1 : idx;
	}

	/**
	 * Returns true when there is a product in this index with a process and flow of
	 * the given IDs.
	 */
	public boolean contains(long processID, long flowID) {
		return getProvider(processID, flowID) != null;
	}

	@Override
	public void each(IndexConsumer<TechFlow> fn) {
		for (int i = 0; i < providers.size(); i++) {
			fn.accept(i, providers.get(i));
		}
	}

	public TechFlow getProvider(long processID, long flowID) {
		List<TechFlow> list = processProviders.get(processID);
		if (list == null)
			return null;
		for (TechFlow p : list) {
			if (p.flowId() == flowID)
				return p;
		}
		return null;
	}

	/**
	 * Adds the given provider (product-output or waste-input) to this index and
	 * returns its position. If the product is already contained in this index its
	 * current position is returned.
	 */
	@Override
	public int add(TechFlow provider) {
		var existing = index.get(provider);
		if (existing != null)
			return existing;
		int pos = index.size();
		index.put(provider, pos);
		var list = processProviders.get(provider.processId());
		if (list == null) {
			list = new ArrayList<>();
			processProviders.put(provider.processId(), list);
		}
		list.add(provider);
		providers.add(provider);
		return pos;
	}

	/**
	 * Returns the provider (product-output or waste-input) at the given index.
	 */
	@Override
	public TechFlow at(int index) {
		return providers.get(index);
	}

	/**
	 * Get all providers with the given descriptor of a process or product system as
	 * entity.
	 */
	public List<TechFlow> getProviders(CategorizedDescriptor d) {
		return d == null
				? Collections.emptyList()
				: getProviders(d.id);
	}

	/**
	 * Returns the providers (product-outputs and waste-inputs) for the process with
	 * the given ID.
	 */
	public List<TechFlow> getProviders(long processId) {
		var providers = processProviders.get(processId);
		return providers == null
				? Collections.emptyList()
				: providers;
	}

	/**
	 * Returns true when there is a process or product system with the given ID part
	 * of this index.
	 */
	public boolean isProvider(long processID) {
		return processProviders.containsKey(processID);
	}

	/**
	 * Adds a process link to this index.
	 *
	 * @param exchange The linked product-input or waste-output as (processId,
	 *                 exchangeId) pair.
	 * @param provider The product-output or waste-input (provider) as (processId,
	 *                 flowId) pair.
	 */
	public void putLink(LongPair exchange, TechFlow provider) {
		if (links.containsKey(exchange))
			return;
		add(provider);
		links.put(exchange, provider);
	}

	@Override
	public TechFlow providerOf(CalcExchange e) {
		return getLinkedProvider(LongPair.of(e.processId, e.exchangeId));
	}

	/**
	 * Returns true if this index also contains the links between processes.
	 */
	public boolean hasLinks() {
		return links.size() != 0;
	}

	/**
	 * Returns the linked provider (product-output or waste-input) for the given
	 * exchange (product-input or waste-output)
	 */
	public TechFlow getLinkedProvider(LongPair exchange) {
		return links.get(exchange);
	}

	/**
	 * Returns all exchanges (product-inputs and waste-outputs) that are linked to
	 * provider of this index.
	 */
	public Set<LongPair> getLinkedExchanges() {
		return links.keySet();
	}

	/**
	 * Returns the IDs of all processes in this index (note that this can also can
	 * contain product system IDs if there are sub-systems in the product system).
	 */
	public Set<Long> getProcessIds() {
		HashSet<Long> set = new HashSet<>();
		for (TechFlow p : providers) {
			set.add(p.processId());
		}
		return set;
	}

	/**
	 * Returns all providers of this index.
	 */
	@Override
	public Set<TechFlow> content() {
		return new HashSet<>(providers);
	}

	@Override
	public TechFlowIndex copy() {
		var copy = new TechFlowIndex(at(0));
		copy.demand = demand;
		for (var p : providers) {
			copy.add(p);
		}
		if (!links.isEmpty()) {
			copy.links.putAll(links);
		}
		return copy;
	}

	@Override
	public Iterator<TechFlow> iterator() {
		return Collections.unmodifiableList(providers).iterator();
	}
}
