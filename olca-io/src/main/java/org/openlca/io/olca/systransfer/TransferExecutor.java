package org.openlca.io.olca.systransfer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.openlca.commons.Res;
import org.openlca.core.matrix.ProductSystemBuilder;
import org.openlca.core.matrix.linking.LinkingConfig;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.ProviderType;

public class TransferExecutor {

	private final TransferPlan plan;

	private TransferExecutor(TransferPlan plan) {
		this.plan = plan;
	}

	public static TransferExecutor of(TransferPlan plan) {
		return new TransferExecutor(plan);
	}

	public Res<ProductSystem> execute() {
		var res = TransferSession.create(plan);
		if (res.isError())
			return res.castError();
		var session = res.value();
		var ctx = session.context();

		session.transferCopies();

		var origin = plan.config().system();
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

		var matches = new HashSet<Long>();
		for (var match : plan.matches()) {
			if (match.selected() != null || match.selected().provider() != null) {
				matches.add(match.selected().provider().id);
			}
		}

		var seq = ctx.seq();
		var queue = new ArrayDeque<ProviderFlow>();
		queue.add(ProviderFlow.rootOf(origin));
		var visited = new HashSet<ProviderFlow>();
		var completionPoints = new HashSet<Long>();
		while (!queue.isEmpty()) {
			var p = queue.poll();
			visited.add(p);
			long targetId = seq.get(p.type, p.provider);
			if (targetId == 0)
				continue;
			copy.processes.add(targetId);
			if (matches.contains(targetId)) {
				completionPoints.add(targetId);
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

		var db = plan.config().target();
		copy = db.insert(copy);
		var builder = new ProductSystemBuilder(db, new LinkingConfig());
		for (var id : completionPoints) {
			// TODO: we need tech-flows for the completion points here
			// also, the completion should only run for processes
			// and maybe with a better way than calling n times
			// builder.autoComplete();
		}

		// TODO transfer parameters, parameter sets, analysis groups

		// TODO
		// - traverse the product system in the same way as in the TransferPlan.PlanBuilder
		// - while traversing complete the new product system
		// - copy required for foreground processes
		// - link matched providers
		// - autocomplete the system for the matched providers
		// - copy analysis groups, parameter sets (including global parameters)

		return Res.error("Not yet implemented");
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
