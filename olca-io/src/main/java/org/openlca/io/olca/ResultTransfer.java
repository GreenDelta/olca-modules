package org.openlca.io.olca;

import java.util.Objects;

import org.openlca.core.model.FlowResult;
import org.openlca.core.model.Result;

final class ResultTransfer implements EntityTransfer<Result> {

	private final TransferConfig conf;

	ResultTransfer(TransferConfig conf) {
		this.conf = conf;
	}

	@Override
	public void syncAll() {
		for (var d : conf.source().getDescriptors(Result.class)) {
			var origin = conf.source().get(Result.class, d.id);
			sync(origin);
		}
	}

	@Override
	public Result sync(Result origin) {
		return conf.sync(origin, () -> {
			var copy = origin.copy();
			copy.impactMethod = conf.swap(origin.impactMethod);
			copy.productSystem = conf.swap(origin.productSystem);
			if (copy.referenceFlow != null) {
				swapRefsOf(copy.referenceFlow);
			}
			for (var e : copy.flowResults) {
				if (Objects.equals(e, copy.referenceFlow))
					continue;
				swapRefsOf(e);
			}
			for (var i : copy.impactResults) {
				i.indicator = conf.swap(i.indicator);
			}
			return copy;
		});
	}

	private void swapRefsOf(FlowResult e) {
		e.flow = conf.swap(e.flow);
		e.flowPropertyFactor = conf.mapFactor(e.flow, e.flowPropertyFactor);
		e.unit = conf.mapUnit(e.flowPropertyFactor, e.unit);
		e.location = conf.swap(e.location);
	}
}
