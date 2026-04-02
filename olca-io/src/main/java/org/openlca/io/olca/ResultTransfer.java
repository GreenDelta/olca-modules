package org.openlca.io.olca;

import java.util.Objects;

import org.openlca.core.model.FlowResult;
import org.openlca.core.model.Result;

final class ResultTransfer implements EntityTransfer<Result> {

	private final TransferContext ctx;

	ResultTransfer(TransferContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void syncAll() {
		for (var d : ctx.source().getDescriptors(Result.class)) {
			var origin = ctx.source().get(Result.class, d.id);
			sync(origin);
		}
	}

	@Override
	public Result sync(Result origin) {
		return ctx.sync(origin, () -> {
			var copy = origin.copy();
			copy.impactMethod = ctx.swap(origin.impactMethod);
			copy.productSystem = ctx.swap(origin.productSystem);
			if (copy.referenceFlow != null) {
				swapRefsOf(copy.referenceFlow);
			}
			for (var e : copy.flowResults) {
				if (Objects.equals(e, copy.referenceFlow))
					continue;
				swapRefsOf(e);
			}
			for (var i : copy.impactResults) {
				i.indicator = ctx.swap(i.indicator);
			}
			return copy;
		});
	}

	private void swapRefsOf(FlowResult e) {
		e.flow = ctx.swap(e.flow);
		e.flowPropertyFactor = ctx.mapFactor(e.flow, e.flowPropertyFactor);
		e.unit = ctx.mapUnit(e.flowPropertyFactor, e.unit);
		e.location = ctx.swap(e.location);
	}
}
