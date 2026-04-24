package org.openlca.io.olca;

import java.util.Objects;

import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;

final class ProjectTransfer implements EntityTransfer<Project> {

	private final TransferContext ctx;

	ProjectTransfer(TransferContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void syncAll() {
		for (var d : ctx.source().getDescriptors(Project.class)) {
			var origin = ctx.source().get(Project.class, d.id);
			sync(origin);
		}
	}

	@Override
	public Project sync(Project origin) {
		return ctx.sync(origin, () -> {
			var copy = origin.copy();
			copy.impactMethod = ctx.resolve(origin.impactMethod);
			if (copy.impactMethod != null && origin.nwSet != null) {
				copy.nwSet = copy.impactMethod.nwSets.stream()
						.filter(nws -> Objects.equals(nws.refId, origin.nwSet.refId))
						.findFirst()
						.orElse(null);
				if (copy.nwSet == null) {
					ctx.log().error("could not map NW set "
							+ origin.nwSet.refId + " in project " + origin.refId);
				}
			}
			for (var variant : copy.variants) {
				swapRefsOf(variant);
			}
			return copy;
		});
	}

	private void swapRefsOf(ProjectVariant variant) {
		variant.productSystem = ctx.resolve(variant.productSystem);
		swapPropertyOf(variant);
		variant.unit = ctx.mapUnit(variant.flowPropertyFactor, variant.unit);
		for (var param : variant.parameterRedefs) {
			if (param.contextId == null)
				continue;
			param.contextId = ctx.seq().get(param.contextType, param.contextId);
		}
	}

	private void swapPropertyOf(ProjectVariant variant) {
		if (variant.flowPropertyFactor == null)
			return;
		var system = variant.productSystem;
		if (system == null || system.referenceExchange == null) {
			variant.flowPropertyFactor = null;
			return;
		}
		var flow = system.referenceExchange.flow;
		variant.flowPropertyFactor = ctx.mapFactor(
				flow, variant.flowPropertyFactor);
	}
}
