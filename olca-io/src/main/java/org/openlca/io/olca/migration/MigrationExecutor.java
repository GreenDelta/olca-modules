package org.openlca.io.olca.migration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.openlca.commons.Res;
import org.openlca.core.matrix.ProductSystemBuilder;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.ProviderType;

public class MigrationExecutor {

	private final MigrationPlan plan;
	private final MigrationConfig config;

	private MigrationExecutor(MigrationPlan plan, MigrationConfig config) {
		this.plan = plan;
		this.config = config;
	}

	public static MigrationExecutor of(MigrationPlan plan, MigrationConfig config) {
		return new MigrationExecutor(plan, config);
	}

	public Res<ProductSystem> execute() {

		// initialize the transfer session
		var res = MigrationSession.create(plan, config);
		if (res.isError())
			return res.castError();
		var session = res.value();
		var ctx = session.context();

		// for now, we just copy all global parameters that are not
		// present yet, later we may only transfer the used ones
		ctx.getTransfer(ModelType.PARAMETER).syncAll();

		// copy the providers that are not mapped, to the target
		// database
		session.transferCopies();

		// initialize the product system copy
		var origin = config.system();
		var copy = origin.copy();
		copy.processLinks.clear();
		copy.analysisGroups.clear();
		copy.processes.clear();
		SystemTransferUtil.swapQRef(ctx, origin, copy);

		var linkIdx = new HashMap<Long, List<ProcessLink>>();
		for (var link : origin.processLinks) {
			linkIdx.computeIfAbsent(link.processId, $ -> new ArrayList<>())
				.add(link);
		}

		var matches = new HashMap<Long, TechFlow>();
		for (var match : plan.matches()) {
			if (!match.isComplete())
				continue;
			var techFlow = TechFlow.of(
				match.selected().provider(), match.selected().flow());
			matches.put(techFlow.providerId(), techFlow);
		}

		var seq = ctx.seq();
		var queue = new ArrayDeque<ProviderFlow>();
		queue.add(ProviderFlow.rootOf(origin));
		var visited = new HashSet<ProviderFlow>();
		var usedMatches = new HashSet<TechFlow>();

		while (!queue.isEmpty()) {
			var p = queue.poll();
			visited.add(p);
			long targetId = seq.get(p.type, p.provider);
			if (targetId == 0)
				continue;
			copy.processes.add(targetId);
			var match = matches.get(targetId);
			if (match != null) {
				usedMatches.add(match);
				continue;
			}

			var links = linkIdx.get(p.provider);
			if (links == null)
				continue;
			for (var link : links) {
				copy.processLinks.add(session.copyLink(link));
				var next = ProviderFlow.of(link);
				if (!visited.contains(next) && !queue.contains(next)) {
					queue.add(next);
				}
			}
		}

		// auto-complete matched processes
		var completionPoints = usedMatches.stream()
			.filter(tf -> tf.isProcess() && !tf.isFromLibrary())
			.toList();
		if (!completionPoints.isEmpty()) {
			new ProductSystemBuilder(config.target())
				.autoComplete(copy, completionPoints);
		}

		// copy analysis groups & insert the system
		session.copyAnalysisGroups(origin, copy);
		copy = config.target().insert(copy);
		return Res.ok(copy);
	}

	private record ProviderFlow(long provider, ModelType type) {

		static ProviderFlow rootOf(ProductSystem system) {
			return system != null && system.referenceProcess != null
				? new ProviderFlow(system.referenceProcess.id, ModelType.PROCESS)
				: new ProviderFlow(0, ModelType.PROCESS);
		}

		static ProviderFlow of(ProcessLink link) {
			if (link == null)
				return new ProviderFlow(0, ModelType.PROCESS);
			var type = ProviderType.toModelType(link.providerType);
			return new ProviderFlow(link.providerId, type);
		}
	}
}
