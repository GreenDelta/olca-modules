package org.openlca.core.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ProductSystem;

class ParameterRedefs {

	private ParameterRedefs() {
	}

	/**
	 * Adds the parameter redefinitions of the given product system to the parameter
	 * redefinitions of the setup. Typically, the given system is a subsystem. It
	 * takes the parameter redefinitions of the baseline parameter set of that
	 * system and adds them to the calculation setup but only if an equivalent
	 * parameter is not already redefined there.
	 */
	static void addTo(CalculationSetup setup, ProductSystem system) {
		if (setup == null || system == null)
			return;
		var baseline = system.parameterSets
			.stream()
			.filter(ps -> ps.isBaseline)
			.findFirst()
			.orElse(system.parameterSets.isEmpty()
				? null
				: system.parameterSets.get(0));
		if (baseline == null || baseline.parameters.isEmpty())
			return;
		var setupParams = setup.parameters();
		if (setupParams.isEmpty()) {
			setup.withParameters(baseline.parameters);
			return;
		}
		var nextParams = new ArrayList<>(setupParams);
		for (var param : baseline.parameters) {
			if (!contains(nextParams, param)) {
				nextParams.add(param);
			}
		}
		setup.withParameters(nextParams);
	}

	/**
	 * Returns true if the given list of contains a redefinition of exactly the same
	 * parameter as the given redefinition.
	 */
	private static boolean contains(
		List<ParameterRedef> list, ParameterRedef redef) {
		for (ParameterRedef listRedef : list) {
			if (same(listRedef, redef))
				return true;
		}
		return false;
	}

	/**
	 * Returns true when the given parameter redefinitions are related to exactly
	 * the same parameter. This is the case when the context is the same and the
	 * name (where parameter names are case-insensitive). An input of invalid
	 * parameters (null or no name) will always return false.
	 */
	private static boolean same(ParameterRedef p1, ParameterRedef p2) {
		if (p1 == null || p2 == null)
			return false;
		if (p1.name == null || p2.name == null)
			return false;
		if (!Objects.equals(p1.contextId, p2.contextId))
			return false;
		if (!Objects.equals(p1.contextType, p2.contextType))
			return false;
		return p1.name.trim().equalsIgnoreCase(p2.name.trim());
	}

}
