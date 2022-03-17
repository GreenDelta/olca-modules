package org.openlca.jsonld.output;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.util.Formula;

/**
 * When global parameters are used in formulas or parameter redefinitions, we
 * need to export them too and this is what the utility functions in this class
 * do.
 */
class GlobalParameters {

	private GlobalParameters() {
	}

	/**
	 * Export redefined global parameters if necessary.
	 */
	public static void sync(Project p, JsonExport exp) {
		if (skipSync(exp))
			return;
		var names = new HashSet<String>();
		for (var variant : p.variants) {
			names.addAll(globalsOf(variant.parameterRedefs));
		}
		writeGlobals(names, exp);
	}

	/**
	 * Export redefined global parameters if necessary.
	 */
	public static void sync(ProductSystem sys, JsonExport exp) {
		if (skipSync(exp))
			return;
		var names = new HashSet<String>();
		for (var set : sys.parameterSets) {
			names.addAll(globalsOf(set.parameters));
		}
		writeGlobals(names, exp);
	}

	/**
	 * Get the names of global parameters from the given redefinitions.
	 */
	private static Set<String> globalsOf(List<ParameterRedef> redefs) {
		return redefs.stream()
				.filter(p -> p.name != null && p.contextId == null)
				.map(p -> p.name)
				.collect(Collectors.toSet());
	}

	/**
	 * Exports global parameters that are referenced from the formulas in the given
	 * process if necessary.
	 */
	public static void sync(Process p, JsonExport exp) {
		if (skipSync(exp))
			return;
		var names = new HashSet<String>();
		for (Exchange e : p.exchanges) {
			names.addAll(Formula.getVariables(e.formula));
			names.addAll(Formula.getVariables(e.costFormula));
			names.addAll(variablesOf(e.uncertainty));
		}
		names.addAll(variablesOf(p.parameters));
		filterLocals(names, p.parameters);
		writeGlobals(names, exp);
	}

	/**
	 * Exports global parameters that are referenced from the formulas in the given
	 * impact category if necessary.
	 */
	public static void sync(ImpactCategory impact, JsonExport exp) {
		if (skipSync(exp))
			return;
		var names = new HashSet<String>();
		for (var factor : impact.impactFactors) {
			names.addAll(Formula.getVariables(factor.formula));
			names.addAll(variablesOf(factor.uncertainty));
		}
		names.addAll(variablesOf(impact.parameters));
		filterLocals(names, impact.parameters);
		writeGlobals(names, exp);
	}

	/**
	 * Exports global parameters that are referenced from the formula of the given
	 * parameter if necessary.
	 */
	public static void sync(Parameter p, JsonExport exp) {
		if (skipSync(exp) || p.isInputParameter)
			return;
		var names = new HashSet<String>();
		names.addAll(Formula.getVariables(p.formula));
		names.addAll(variablesOf(p.uncertainty));
		writeGlobals(names, exp);
	}

	private static Set<String> variablesOf(List<Parameter> params) {
		var names = new HashSet<String>();
		for (var param : params) {
			// no formulas in input parameters
			if (param.isInputParameter)
				continue;
			names.addAll(Formula.getVariables(param.formula));
			names.addAll(variablesOf(param.uncertainty));
		}
		return names;
	}

	/**
	 * Removes the parameters from the names that are already defined in the given
	 * parameter list.
	 */
	private static void filterLocals(Set<String> names, List<Parameter> locals) {
		var localNames = locals.stream()
				.filter(p -> p.name != null)
				.map(p -> p.name.trim().toLowerCase())
				.collect(Collectors.toSet());
		names.removeIf(name -> localNames.contains(
				name.trim().toLowerCase()));
	}

	private static Set<String> variablesOf(Uncertainty u) {
		Set<String> names = new HashSet<>();
		if (u == null)
			return names;
		if (u.distributionType == null)
			return names;
		if (u.distributionType == UncertaintyType.NONE)
			return names;
		names.addAll(Formula.getVariables(u.formula1));
		names.addAll(Formula.getVariables(u.formula2));
		if (u.distributionType == UncertaintyType.TRIANGLE) {
			names.addAll(Formula.getVariables(u.formula3));
		}
		return names;
	}

	private static void writeGlobals(Set<String> names, JsonExport exp) {
		if (names.isEmpty())
			return;

		// load all global parameters
		Map<String, Parameter> globals = new ParameterDao(exp.db)
				.getGlobalParameters()
				.stream()
				.filter(p -> p.name != null)
				.collect(Collectors.toMap(
						p -> p.name.trim().toLowerCase(),
						p -> p));

		for (String name : names) {
			var g = globals.get(name.trim().toLowerCase());
			if (g == null)
				continue;
			exp.write(g);
			// the global parameter could have again a formula
			// that contains references to other global parameters.
			sync(g, exp);
		}
	}

	private static boolean skipSync(JsonExport exp) {
		return exp == null
				|| !exp.exportReferences
				|| exp.db == null;
	}
}
