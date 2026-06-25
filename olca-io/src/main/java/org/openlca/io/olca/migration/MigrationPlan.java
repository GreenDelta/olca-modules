package org.openlca.io.olca.migration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openlca.commons.Res;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProviderType;
import org.openlca.core.model.descriptors.RootDescriptor;

public record MigrationPlan(
	List<ProductSystem> systems,
	List<ProviderMatch> providerMatches,
	List<ProviderInfo> providerCopies,
	List<RootDescriptor> entityCopies,
	List<RootDescriptor> entityMatches
) {

	public static Res<MigrationPlan> createFrom(MigrationConfig config) {
		if (config == null || config.isNotComplete())
			return Res.error("No valid migration configuration provided");
		try {
			return new PlanBuilder(config).build();
		} catch (Exception e) {
			return Res.error("Failed to create migration plan", e);
		}
	}

	private static class PlanBuilder {

		private final MigrationConfig config;
		private final MigrationPlan plan;


		PlanBuilder(MigrationConfig config) {
			this.config = config;
			this.plan = new MigrationPlan(
				new ArrayList<>(),
				new ArrayList<>(),
				new ArrayList<>(),
				new ArrayList<>(),
				new ArrayList<>()
			);
		}

		Res<MigrationPlan> build() {
			var res = initPlan().then(this::checkSystems);
			if (res.isError())
				return res.wrapError("Failed to create migration plan");

			if (!config.allProcesses() && plan.systems.isEmpty())
				return Res.ok(plan);

			var sourceProviders = config.allProcesses()
				? ProviderInfo.allOf(config.source())
				: ProviderInfo.allOf(config.source(), plan.systems);

			var targetIdx = new HashMap<String, List<ProviderInfo>>();
			for (var pi : ProviderInfo.allOf(config.target())) {
				targetIdx
					.computeIfAbsent(pi.flowId(), flowId -> new ArrayList<>())
					.add(pi);
			}

			if (config.allProcesses()) {
				addAllProcessesOf(sourceProviders, targetIdx);
				// now we added all processes, but there could be results linked
				// in product systems that we also need to migrate
				if (plan.systems.isEmpty())
					return checkNoLibraryCopies();
			}

			var tRes = traverseSystems(sourceProviders, targetIdx);
			return tRes.isError()
				? tRes.wrapError("Failed to collect providers from product systems")
				: checkNoLibraryCopies();
		}

		private Res<Void> traverseSystems(
			List<ProviderInfo> sourceProviders,
			Map<String, List<ProviderInfo>> targetIdx
		) {
			var sourceIdx = new HashMap<ProviderFlow, ProviderInfo>();
			for (var pi : sourceProviders) {
				sourceIdx.put(ProviderFlow.of(pi), pi);
			}
			var matchSet = new HashSet<ProviderFlow>();
			var copySet = new HashSet<ProviderFlow>();

			for (var system : plan.systems) {

				var linkIdx = new HashMap<Long, List<ProcessLink>>();
				for (var link : system.processLinks) {
					linkIdx
						.computeIfAbsent(link.processId, processId -> new ArrayList<>())
						.add(link);
				}

				var queue = new ArrayDeque<ProviderFlow>();
				queue.add(ProviderFlow.rootOf(system));
				var visited = new HashSet<ProviderFlow>();

				while (!queue.isEmpty()) {
					var pid = queue.poll();
					visited.add(pid);
					if (matchSet.contains(pid))
						continue;

					var provider = sourceIdx.get(pid);
					if (provider == null)
						return Res.error("Could not find provider for: " + pid);
					var match = matchOf(provider, targetIdx);
					if (match != null) {
						plan.providerMatches.add(match);
						matchSet.add(pid);
						continue;
					}
					if (!copySet.contains(pid)) {
						plan.providerCopies.add(provider);
						copySet.add(pid);
					}

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
			}
			return Res.ok();
		}

		private void addAllProcessesOf(
			List<ProviderInfo> providers, Map<String, List<ProviderInfo>> targetIdx
		) {
			for (var p : providers) {
				if (!p.isProcess())
					continue;
				var match = matchOf(p, targetIdx);
				if (match != null) {
					plan.providerMatches.add(match);
				} else {
					plan.providerCopies.add(p);
				}
			}
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

		private Res<Void> initPlan() {
			var allowedTypes = EnumSet.of(
				ModelType.PROJECT,
				ModelType.PRODUCT_SYSTEM,
				ModelType.IMPACT_METHOD);

			for (var e : config.entities()) {
				if (e == null || e.type == null || e.refId == null)
					return Res.error("Migration configuration contains invalid entities");
				if (e.isFromLibrary())
					return Res.error("Cannot migrate library data: " + e.library);
				if (!allowedTypes.contains(e.type))
					return Res.error("Unsupported root type for migration: " + e.type);

				var d = config.target()
					.getDescriptor(e.type.getModelClass(), e.refId);
				if (d != null) {
					plan.entityMatches.add(d);
					continue;
				}

				if (e.type == ModelType.PROJECT) {
					var project = config.source().get(Project.class, e.id);
					if (project == null)
						return Res.error("Failed to load project: " + e.refId);
					plan.entityCopies.add(e);
					for (var v : project.variants) {
						add(v.productSystem);
					}
					continue;
				}

				if (e.type != ModelType.PRODUCT_SYSTEM) {
					plan.entityCopies.add(e);
					continue;
				}

				var system = config.source().get(ProductSystem.class, e.id);
				if (system == null)
					return Res.error("Failed to load product system: " + e.refId);
				add(system);
			}
			return Res.ok();
		}

		private void add(ProductSystem system) {
			if (system == null || plan.systems.contains(system))
				return;
			plan.systems.add(system);
		}

		private Res<Void> checkSystems() {
			for (var s : plan.systems) {
				for (var link : s.processLinks) {
					if (link.providerType == ProviderType.SUB_SYSTEM)
						return Res.error(
							"The migration of product systems with sub-systems is not "
								+ "supported yet. Sub-system(s) found in product system: "
								+ s.refId);
				}
			}
			return Res.ok();
		}

		private Res<MigrationPlan> checkNoLibraryCopies() {
			for (var p : plan.providerCopies) {
				if (p.provider() != null && p.provider().isFromLibrary()) {
					return Res.error(
						"The migration setup would require to copy library data, "
							+ "e.g. from library " + p.provider().library);
				}
			}
			return Res.ok(plan);
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
