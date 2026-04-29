package org.openlca.io.olca.systransfer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.commons.Res;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;

import gnu.trove.map.hash.TLongObjectHashMap;

public record TransferPlan(
	TransferConfig config,
	List<ProviderMatch> matches,
	List<ProviderInfo> copied
) {

	public static Res<TransferPlan> createFrom(TransferConfig config) {
		if (config == null || config.isNotComplete())
			return Res.error("No valid transfer configuration provided");
		try {
			return new PlanBuilder(config).build();
		} catch (Exception e) {
			return Res.error("Failed to create transfer plan", e);
		}
	}

	private static class PlanBuilder {

		private final TransferConfig config;
		private final ProductSystem system;

		PlanBuilder(TransferConfig config) {
			this.config = config;
			this.system = config.system();
		}

		Res<TransferPlan> build() {

			var links = new TLongObjectHashMap<ProcessLink>();
			for (var link : system.processLinks) {
				links.put(link.processId, link);
			}

			var sourceIdx = new HashMap<ProviderFlow, ProviderInfo>();
			for (var pi : ProviderInfo.allOf(config.source(), system)) {
				sourceIdx.put(ProviderFlow.of(pi), pi);
			}

			var targetIdx = new HashMap<String, List<ProviderInfo>>();
			for (var pi : ProviderInfo.allOf(config.target())) {
				targetIdx
					.computeIfAbsent(pi.flowId(), flowId -> new ArrayList<>())
					.add(pi);
			}

			var queue = new ArrayDeque<ProviderFlow>();
			queue.add(ProviderFlow.rootOf(system));

			while (!queue.isEmpty()) {
				var next = queue.poll();
				var provider = sourceIdx.get(next);
				if (provider == null)
					return Res.error("Could not find provider for: " + next);

			}
			return Res.error("Not yet implemented");
		}

		private ProviderMatch matchOf(
			ProviderInfo source, Map<String, List<ProviderInfo>> target
		) {
			var candidates = target.get(source.flowId());
			if (candidates.isEmpty())
				return null;

			for (var c : candidates) {

			}

			return null;
		}

	}

	private record ProviderFlow(long provider, long flow) {

		static ProviderFlow rootOf(ProductSystem system) {
			if (system == null
				|| system.referenceProcess == null
				|| system.referenceExchange == null
				|| system.referenceExchange.flow == null)
				return new ProviderFlow(0, 0);
			return new ProviderFlow(
				system.referenceProcess.id, system.referenceExchange.flow.id);
		}

		static ProviderFlow of(ProviderInfo info) {
			return info != null && info.provider() != null && info.flow() != null
				? new ProviderFlow(info.provider().id, info.flow().id)
				: new ProviderFlow(0, 0);
		}
	}

}
