package org.openlca.core.math;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ProductSystem;

class ParameterRedefs {

	private ParameterRedefs() {
	}

	/**
	 * Combines the parameters of the calculation setup with parameters of the
	 * baseline parameter set of the given product system. If the same parameter
	 * is (re-) defined in the calculation setup and product system it is taken
	 * from the calculation setup unless the parameter of the product system is
	 * set as {@code protected}. This function is typically used to apply a
	 * parameter set from a calculation setup on a sub-system.
	 */
	static List<ParameterRedef> join(CalculationSetup setup, ProductSystem system) {
		var baseline = system.parameterSets
			.stream()
			.filter(ps -> ps.isBaseline)
			.findFirst()
			.orElse(system.parameterSets.isEmpty()
				? null
				: system.parameterSets.get(0));

		if (baseline == null || baseline.parameters.isEmpty())
			return setup.parameters();

		var systemParams = baseline.parameters;
		var setupParams = setup.parameters();

		if (setupParams.isEmpty())
			return systemParams;

		var params = new ArrayList<ParameterRedef>();
		var added = new HashSet<String>();

		// add the protected system parameters
		for (var p : systemParams) {
			if (p.isProtected) {
				added.add(keyOf(p));
				params.add(p);
			}
		}

		Consumer<List<ParameterRedef>> next = nextParams -> {
			for (var p : nextParams) {
				var key = keyOf(p);
				if (added.contains(key))
					continue;
				added.add(key);
				params.add(p);
			}
		};

		// add the setup parameters
		next.accept(setupParams);

		// finally, add the system parameters that
		// were not already added
		next.accept(systemParams);

		return params;
	}

	private static String keyOf(ParameterRedef p) {
		var k = p.name == null
			? ""
			: p.name.trim().toLowerCase();
		return p.contextId != null
			? k + p.contextId + p.contextType
			: k;
	}
}
