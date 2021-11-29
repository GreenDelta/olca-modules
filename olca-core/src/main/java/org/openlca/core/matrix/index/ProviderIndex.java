package org.openlca.core.matrix.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;

public abstract class ProviderIndex {

	protected final IDatabase db;
	protected final TLongObjectHashMap<ProcessDescriptor> processes;
	protected final TLongObjectHashMap<ProductSystemDescriptor> systems;
	protected final TLongObjectHashMap<FlowDescriptor> flows;
	protected final TLongObjectHashMap<List<CategorizedDescriptor>> providers;

	private ProviderIndex(IDatabase db) {
		this.db = db;
		processes = new ProcessDao(db).descriptorMap();
		systems = new ProductSystemDao(db).descriptorMap();
		var flowDescriptors = new FlowDao(db).getDescriptors(
			FlowType.PRODUCT_FLOW, FlowType.WASTE_FLOW);
		flows = new TLongObjectHashMap<>(flowDescriptors.size());
		for (var f : flowDescriptors) {
			flows.put(f.id, f);
		}
		providers = new TLongObjectHashMap<>();
	}

	public static ProviderIndex eager(IDatabase db) {
		return new EagerIndex(db);
	}

	public static ProviderIndex lazy(IDatabase db) {
		return new LazyIndex(db);
	}

	/**
	 * Get the TechFlow for the given provider and flow IDs.
	 *
	 * @param providerId the ID of the process or product system
	 * @param flowId     the ID of the product or waste flow
	 * @return the corresponding TechFlow or {@code null} if there is
	 * no such provider in the database.
	 */
	public TechFlow of(long providerId, long flowId) {
		var flow = flows.get(flowId);
		if (flow == null)
			return null;
		var process = processes.get(providerId);
		if (process != null)
			return TechFlow.of(process, flow);
		var system = systems.get(providerId);
		return system != null
			? TechFlow.of(system, flow)
			: null;
	}

	public abstract List<TechFlow> getProvidersOf(long flowId);

	private static class LazyIndex extends ProviderIndex {

		LazyIndex(IDatabase db) {
			super(db);
		}

		@Override
		public List<TechFlow> getProvidersOf(long flowId) {
			var flow = flows.get(flowId);
			if (flow == null)
				return Collections.emptyList();
			var ps = providers.get(flowId);
			if (ps != null)
				return ps.stream()
					.map(p -> TechFlow.of(p, flow))
					.collect(Collectors.toList());

			// select from processes
			var sql = NativeSql.on(db);
			var flowProviders = new ArrayList<CategorizedDescriptor>();
			boolean forInputs = flow.flowType == FlowType.WASTE_FLOW;
			var processQuery = "select f_owner from tbl_exchanges where "
				+ " f_flow = " + flowId + " and is_input = " + forInputs;
			sql.query(processQuery, r -> {
				var process = processes.get(r.getLong(1));
				if (process != null) {
					flowProviders.add(process);
				}
				return true;
			});

			// select from product systems
			var systemQuery = "select s.id from tbl_product_systems s "
				+ "inner join tbl_exchanges e "
				+ "on s.f_reference_exchange = e.id "
				+ "where e.f_flow = " + flowId ;

			sql.query(systemQuery, r -> {
				var system = systems.get(r.getLong(1));
				if (system != null) {
					flowProviders.add(system);
				}
				return true;
			});

			providers.put(flowId, flowProviders);

			return flowProviders.stream()
				.map(p -> TechFlow.of(p, flow))
				.collect(Collectors.toList());
		}
	}

	private static class EagerIndex extends ProviderIndex {

		EagerIndex(IDatabase db) {
			super(db);
			var sql = NativeSql.on(db);

			// select from processes
			var processQuery = "select f_owner, f_flow, is_input from tbl_exchanges";
			sql.query(processQuery, r -> {
				var flowId = r.getLong(2);
				var isInput = r.getBoolean(3);
				if (!isProviderFlow(flowId, isInput))
					return true;
				var process = processes.get(r.getLong(1));
				if (process == null)
					return true;
				var ps = providers.get(flowId);
				if (ps == null) {
					ps = new ArrayList<>();
					providers.put(flowId, ps);
				}
				ps.add(process);
				return true;
			});

			// select product systems
			var systemQuery = """
				select s.id, e.f_flow from tbl_product_systems s
						inner join tbl_exchanges e
						on s.f_reference_exchange = e.id
				""";
			sql.query(systemQuery, r -> {
				var flowId = r.getLong(1);
				var ps = providers.get(flowId);
				if (ps == null) // must exist if the database correct
					return true;
				var system = systems.get(r.getLong(1));
				if (system != null) {
					ps.add(system);
				}
				return true;
			});

		}

		protected boolean isProviderFlow(long flowId, boolean isInput) {
			var flow = flows.get(flowId);
			if (flow == null)
				return false;
			return (isInput && flow.flowType == FlowType.WASTE_FLOW)
				|| (!isInput && flow.flowType == FlowType.PRODUCT_FLOW);
		}

		@Override
		public List<TechFlow> getProvidersOf(long flowId) {
			var flow = flows.get(flowId);
			if (flow == null)
				return Collections.emptyList();
			var ps = providers.get(flowId);
			if (ps == null)
				return Collections.emptyList();
			return ps.stream().map(p -> TechFlow.of(p, flow))
				.collect(Collectors.toList());
		}
	}


}
