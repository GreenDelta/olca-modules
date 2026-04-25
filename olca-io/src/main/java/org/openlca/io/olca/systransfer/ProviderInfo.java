package org.openlca.io.olca.systransfer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ResultDao;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ResultDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;

import gnu.trove.map.hash.TLongObjectHashMap;

public record ProviderInfo(
	FlowDescriptor flow,
	RootDescriptor provider,
	LocationDescriptor location
) {

	static List<ProviderInfo> allOf(IDatabase db) {
		return allOf(db, null);
	}

	static List<ProviderInfo> allOf(IDatabase db, ProviderFilter filter) {
		return new Scan(db, filter).collect();
	}

	String flowId() {
		return flow != null ? flow.refId : null;
	}

	private static final class Scan {

		private final ProviderFilter filter;
		private final NativeSql sql;
		private final TLongObjectHashMap<ProcessDescriptor> processes;
		private final TLongObjectHashMap<ResultDescriptor> results;
		private final Map<Long, FlowDescriptor> flows;
		private final TLongObjectHashMap<LocationDescriptor> locations;

		private Scan(IDatabase db, ProviderFilter filter) {
			this.filter = filter;
			sql = NativeSql.on(db);
			processes = new ProcessDao(db).descriptorMap();
			results = new ResultDao(db).descriptorMap();
			flows = new FlowDao(db)
				.getDescriptors(FlowType.PRODUCT_FLOW, FlowType.WASTE_FLOW)
				.stream()
				.collect(Collectors.toMap(d -> d.id, d -> d, (a, b) -> a));
			locations = new LocationDao(db).descriptorMap();
		}

		private List<ProviderInfo> collect() {
			var candidates = new ArrayList<ProviderInfo>();
			processes(candidates);
			results(candidates);
			return candidates;
		}

		private void processes(List<ProviderInfo> candidates) {
			sql.query("select f_owner, f_flow, is_input from tbl_exchanges", r -> {
				long pid = r.getLong(1);
				if (filter != null && !filter.containsProcess(pid))
					return true;
				var provider = processes.get(pid);
				var flow = flows.get(r.getLong(2));
				if (provider == null || skipFlow(flow, r.getBoolean(3)))
					return true;

				var location = provider.location != null
					? locations.get(provider.location)
					: null;
				candidates.add(new ProviderInfo(flow, provider, location));
				return true;
			});
		}

		private void results(List<ProviderInfo> candidates) {
			sql.query("select f_result, f_flow, is_input from tbl_flow_results", r -> {
				var pid = r.getLong(1);
				if (filter != null && !filter.containsResult(pid))
					return true;
				var provider = results.get(pid);
				var flow = flows.get(r.getLong(2));
				if (provider == null || skipFlow(flow, r.getBoolean(3)) )
					return true;
				candidates.add(new ProviderInfo(flow, provider, null));
				return true;
			});
		}

		private boolean skipFlow(FlowDescriptor flow, boolean isInput) {
			if (flow == null || flow.flowType == null)
				return true;
			return switch (flow.flowType) {
				case PRODUCT_FLOW -> isInput;
				case WASTE_FLOW -> !isInput;
				default -> true;
			};
		}
	}
}
