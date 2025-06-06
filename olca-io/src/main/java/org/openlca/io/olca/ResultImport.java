package org.openlca.io.olca;

import java.util.Objects;

import org.openlca.core.model.FlowResult;
import org.openlca.core.model.Result;

class ResultImport {

	private final Config conf;

	private ResultImport(Config conf) {
		this.conf = conf;
	}

	static void run(Config conf) {
		new ResultImport(conf).run();
	}

	private void run() {
		conf.syncAll(Result.class, result -> {
			var copy = result.copy();
			copy.impactMethod = conf.swap(result.impactMethod);
			copy.productSystem = conf.swap(result.productSystem);
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
