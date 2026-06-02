package org.openlca.io.olca.systransfer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.openlca.commons.Res;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.ProviderType;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.io.olca.ProcessTransfer;
import org.openlca.io.olca.TransferContext;

public class TransferExecutor {

	private final TransferPlan plan;

	private TransferExecutor(TransferPlan plan) {
		this.plan = plan;
	}

	public static TransferExecutor of(TransferPlan plan) {
		return new TransferExecutor(plan);
	}

	public Res<ProductSystem> execute() {
		var res = createContext();
		if (res.isError())
			return res.castError();
		var ctx = res.value();

		for (var p : plan.copies()) {
			if (p.provider() == null || p.provider().type == null)
				continue;
			var entity = ctx.source().get(
				p.provider().type.getModelClass(), p.provider().id);
			ctx.resolve(entity);
		}
		ProcessTransfer.swapDefaultProviders(ctx);

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
		var exchanges = ExchangeFinder.of(ctx);
		while (!queue.isEmpty()) {
			var p = queue.poll();
			visited.add(p);
			long targetId = seq.get(p.type, p.provider);
			if (targetId == 0)
				continue;
			copy.processes.add(targetId);
			if (matches.contains(targetId))
				continue;

			var links = linkIdx.get(p.provider);
			if (links == null)
				continue;
			for (var link : links) {
				var next = ProviderFlow.of(link);

				// TODO: when the provider here is matched, it could have a different
				// type than the original link has!
				var targetLink = link.copy();
				targetLink.providerId = seq.get(next.type, next.provider);
				targetLink.flowId = seq.get(ModelType.FLOW, link.flowId);
				targetLink.processId = seq.get(ModelType.PROCESS, link.processId);
				targetLink.exchangeId = exchanges.find(link);
				copy.processLinks.add(targetLink);

			}
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

	private Res<TransferContext> createContext() {
		if (plan == null
			|| plan.config() == null
			|| plan.config().isNotComplete())
			return Res.error("Incomplete transfer configuration");
		try {
			var ctx = TransferContext.create(
				plan.config().source(), plan.config().target());
			var seq = ctx.seq();
			for (var match : plan.matches()) {
				if (match.provider() == null
					|| match.provider().provider() == null
					|| match.selected().provider() == null)
					continue;
				// TODO: we need to support type switches
				seq.put(
					match.provider().provider().type,
					match.provider().provider().id,
					match.selected().provider().id);
			}
			return Res.ok(ctx);
		} catch (Exception e) {
			return Res.error("Failed to create the transfer context", e);
		}
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
