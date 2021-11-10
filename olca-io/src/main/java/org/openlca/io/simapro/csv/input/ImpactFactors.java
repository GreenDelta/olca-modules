package org.openlca.io.simapro.csv.input;

import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.simapro.csv.enums.SubCompartment;
import org.openlca.simapro.csv.method.ImpactCategoryBlock;
import org.openlca.simapro.csv.method.ImpactFactorRow;

import java.util.EnumMap;
import java.util.HashMap;

class ImpactFactors {

	private ImpactFactors() {
	}

	static void expand(ImpactCategoryBlock block) {
		if (block == null)
			return;

		// build a reverse index: flow -> type -> sub-compartment -> factor row
		var idx = new HashMap<String, EnumMap<ElementaryFlowType, EnumMap<
			SubCompartment, ImpactFactorRow>>>();
		for (var row : block.factors()) {
			var top = ElementaryFlowType.of(row.compartment());
			var sub = SubCompartment.of(row.subCompartment());
			if (top == null || sub == null || row.flow() == null)
				continue;
			var key = row.flow().trim().toLowerCase();
			if (key.isEmpty())
				continue;
			idx.computeIfAbsent(key, _key -> new EnumMap<>(ElementaryFlowType.class))
				.computeIfAbsent(top, _top -> new EnumMap<>(SubCompartment.class))
				.put(sub, row);
		}

		// generate factors
		for (var flowEntry : idx.entrySet()) {
			var map = flowEntry.getValue();
			for (var typeEntry : map.entrySet()) {
				var type = typeEntry.getKey();
				var rows = typeEntry.getValue();
				var unspecified = rows.get(SubCompartment.UNSPECIFIED);
				if (unspecified == null)
					continue;
				for (var sub : SubCompartment.values()) {
					if (sub == SubCompartment.UNSPECIFIED
						|| sub.flowType() != type
						|| rows.containsKey(sub))
						continue;
					block.factors().add(new ImpactFactorRow()
						.compartment(unspecified.compartment())
						.subCompartment(sub.toString())
						.flow(unspecified.flow())
						.casNumber(unspecified.casNumber())
						.factor(unspecified.factor())
						.unit(unspecified.unit()));
				}
			}
		}
	}
}
