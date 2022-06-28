package org.openlca.core.matrix.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.ResultDao;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.TechLinker;
import org.openlca.core.matrix.linking.DefaultProcessLinker;
import org.openlca.core.matrix.linking.LinkingInfo;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.RootDescriptor;

import gnu.trove.map.hash.TLongObjectHashMap;

/**
 *
 * The index of the technology matrix of a product system. It maps the
 * product-outputs and waste-inputs of the respective processes, sub-systems,
 * or results in the system as technosphere-flows to the respective rows and
 * columns.
 */
public final class TechIndex implements TechLinker, MatrixIndex<TechFlow> {

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

	public TechIndex() {
	}

	public TechIndex(TechFlow first) {
		add(first);
	}

	public boolean isEmpty() {
		return index.isEmpty();
	}

	public static TechIndex of(TechFlow... techFlows) {
		var index = new TechIndex();
		if (techFlows == null)
			return index;
		for (var techFlow : techFlows) {
			index.add(techFlow);
		}
		return index;
	}

	/**
	 * Creates the index for the given product system. If the `withoutNetwork`
	 * attribute of the system is set to true, it creates an unlinked index with all
	 * process products (and waste flows) from the database. Otherwise all process
	 * links of the product system are directly stored in the index.
	 */
	public static TechIndex of(IDatabase db, ProductSystem system) {
		var refExchange = Objects.requireNonNull(system.referenceExchange);
		var refFlow = Objects.requireNonNull(refExchange.flow);
		var refProcess = Objects.requireNonNull(system.referenceProcess);
		var index = new TechIndex(TechFlow.of(refProcess, refFlow));
		index.fillFrom(db, system);
		return index;
	}

	public static TechIndex of(IDatabase db, CalculationSetup setup) {
		var process = Objects.requireNonNull(setup.process());
		var flow = Objects.requireNonNull(setup.flow());
		var refFlow = TechFlow.of(process, flow);

		if (setup.hasProductSystem()) {
			var index = new TechIndex(refFlow);
			index.fillFrom(db, setup.productSystem());
			return index;
		}

		// try to decide if we should just link the complete database
		// (e.g. in case of unit process databases) or just link
		// required things
		var linking = LinkingInfo.of(db);
		if (linking.preferLazy()) {
			var linker = DefaultProcessLinker.of(linking);
			return linker.build(refFlow);
		}

		// include all providers from the database
		var index = new TechIndex(refFlow);
		eachProviderOf(db, index::add);
		return index;
	}

	private void fillFrom(IDatabase db, ProductSystem system) {
		var systems = new ProductSystemDao(db).descriptorMap();
		var processes = new ProcessDao(db).descriptorMap();
		var results = new ResultDao(db).descriptorMap();

		var flows = new FlowDao(db).descriptorMap();

		for (var link : system.processLinks) {
			RootDescriptor p = processes.get(link.providerId);
			if (p == null) {
				p = systems.get(link.providerId);
				if (p == null) {
					p = results.get(link.providerId);
				}
			}
			if (p == null)
				continue;

			var flow = flows.get(link.flowId);
			if (flow == null)
				continue;

			// the tech-index checks for duplicates of products and links
			var provider = TechFlow.of(p, flow);
			add(provider);
			var exchange = new LongPair(link.processId, link.exchangeId);
			putLink(exchange, provider);
		}
	}

	/**
	 * Creates an unlinked index of all process products (and waste flows) of the
	 * database in some arbitrary order.
	 */
	public static TechIndex of(IDatabase db) {
		var list = new ArrayList<TechFlow>();
		eachProviderOf(db, list::add);
		var index = new TechIndex();
		for (var techFlow : list) {
			index.add(techFlow);
		}
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

	public TechFlow getProvider(long processId, long flowId) {
		List<TechFlow> list = processProviders.get(processId);
		if (list == null)
			return null;
		for (TechFlow p : list) {
			if (p.flowId() == flowId)
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
		var list = processProviders.get(provider.providerId());
		if (list == null) {
			list = new ArrayList<>();
			processProviders.put(provider.providerId(), list);
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
	public List<TechFlow> getProviders(RootDescriptor d) {
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
			set.add(p.providerId());
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
	public TechIndex copy() {
		var copy = new TechIndex();
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
