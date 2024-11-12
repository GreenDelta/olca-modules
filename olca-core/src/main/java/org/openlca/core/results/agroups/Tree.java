package org.openlca.core.results.agroups;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.results.LcaResult;

record Tree(
		String group,
		TechFlow techFlow,
		int index,
		double scaling,
		double amount,
		List<Tree> childs
) {

	static Tree rootOf(LcaResult result, GroupMap groups) {
		var demand = result.demand();
		var techFlow = result.demand().techFlow();
		var amount = demand.value();
		var index = result.techIndex().of(techFlow);
		double aii = result.provider().techValueOf(index, index);
		return new Tree(
				groups.top(), techFlow, index, amount / aii, amount, new ArrayList<>()
		);
	}

	long pid() {
		return techFlow.providerId();
	}
}
