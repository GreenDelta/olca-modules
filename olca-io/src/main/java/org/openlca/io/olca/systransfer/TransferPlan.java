package org.openlca.io.olca.systransfer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openlca.commons.Res;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;

public record TransferPlan(
	TransferConfig config,
	List<ProviderMatch> matches,
	List<ProviderInfo> copies
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

			var linkIdx = new HashMap<Long, List<ProcessLink>>();
			for (var link : system.processLinks) {
				linkIdx
					.computeIfAbsent(link.processId, processId -> new ArrayList<>())
					.add(link);
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
			var visited = new HashSet<ProviderFlow>();
			var matches = new ArrayList<ProviderMatch>();
			var copies = new ArrayList<ProviderInfo>();

			while (!queue.isEmpty()) {
				var pid = queue.poll();
				visited.add(pid);
				var provider = sourceIdx.get(pid);
				if (provider == null)
					return Res.error("Could not find provider for: " + pid);
				var match = matchOf(provider, targetIdx);
				if (match != null) {
					matches.add(match);
					continue;
				}

				copies.add(provider);
				var links = linkIdx.get(pid.provider);
				if (links == null)
					continue;
				for (var link : links) {
					var next = ProviderFlow.of(link);
					if (!visited.contains(next) && !queue.contains(next)) {
						queue.add(next);
					}
				}
			}

			var plan = new TransferPlan(config, matches, copies);
			return Res.ok(plan);
		}

		private ProviderMatch matchOf(
			ProviderInfo provider, Map<String, List<ProviderInfo>> targetIdx
		) {
			var candidates = targetIdx.get(provider.flowId());
			if (candidates == null || candidates.isEmpty())
				return null;
			for (var strategy : config.strategies()) {
				var match = strategy.matchOf(provider, candidates);
				if (match != null)
					return match;
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

		static ProviderFlow of(ProcessLink link) {
			return link != null
				? new ProviderFlow(link.providerId, link.flowId)
				: new ProviderFlow(0, 0);
		}
	}
}
