package org.openlca.core.results.agroups;

import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.results.LcaResult;

record Node(
	String group,
	TechFlow techFlow,
	int index,
	double scaling,
	double amount
) {

	static Node rootOf(LcaResult result, GroupMap groups) {
		var demand = result.demand();
		var techFlow = result.demand().techFlow();
		var amount = demand.value();
		var index = result.techIndex().of(techFlow);
		double aii = result.provider().techValueOf(index, index);
		return new Node(groups.top(), techFlow, index, amount / aii, amount);
	}

	boolean isFromLibrary() {
		return techFlow.provider().isFromLibrary();
	}

}
