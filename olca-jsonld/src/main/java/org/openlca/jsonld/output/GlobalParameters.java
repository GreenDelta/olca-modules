package org.openlca.jsonld.output;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.Scenario;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.util.Formula;

/**
 * When global parameters are used in formulas or parameter redifinitions, we
 * need to export them too and this is what the utility functions in this class
 * do.
 */
class GlobalParameters {

	private GlobalParameters() {
	}

	/**
	 * Export redefined global parameters if necessary.
	 */
	public static void sync(Project p, ExportConfig conf) {
		if (skipSync(conf))
			return;
		Set<String> names = new HashSet<>();
		for (ProjectVariant v : p.variants)
			names.addAll(getGlobals(v.parameterRedefs));
		writeGlobals(names, conf);
	}

	/**
	 * Export redefined global parameters if necessary.
	 */
	public static void sync(ProductSystem s, ExportConfig conf) {
		if (skipSync(conf))
			return;
		Set<String> names = new HashSet<>();
		names.addAll(getGlobals(s.parameterRedefs));
		for (Scenario scenario : s.scenarios) {
			names.addAll(getGlobals(scenario.parameters));
		}
		writeGlobals(names, conf);
	}

	/**
	 * Get the names of global parameters from the given redefinitions.
	 */
	private static Set<String> getGlobals(List<ParameterRedef> redefs) {
		return redefs.stream()
				.filter(p -> p.name != null && p.contextId == null)
				.map(p -> p.name)
				.collect(Collectors.toSet());
	}

	/**
	 * Exports global parameters that are referenced from the formulas in the given
	 * process if necessary.
	 */
	public static void sync(Process p, ExportConfig conf) {
		if (skipSync(conf))
			return;
		Set<String> names = new HashSet<>();
		for (Exchange e : p.exchanges) {
			names.addAll(Formula.getVariables(e.formula));
			names.addAll(Formula.getVariables(e.costFormula));
			names.addAll(getFormulaVariables(e.uncertainty));
		}
		names.addAll(getFormulaVariables(p.parameters));
		filterLocals(names, p.parameters);
		writeGlobals(names, conf);
	}

	/**
	 * Exports global parameters that are referenced from the formulas in the given
	 * impact category if necessary.
	 */
	public static void sync(ImpactCategory impact, ExportConfig conf) {
		if (skipSync(conf))
			return;
		Set<String> names = new HashSet<>();
		for (ImpactFactor f : impact.impactFactors) {
			names.addAll(Formula.getVariables(f.formula));
			names.addAll(getFormulaVariables(f.uncertainty));
		}
		names.addAll(getFormulaVariables(impact.parameters));
		filterLocals(names, impact.parameters);
		writeGlobals(names, conf);
	}

	/**
	 * Exports global parameters that are referenced from the formula of the given
	 * parameter if necessary.
	 */
	public static void sync(Parameter p, ExportConfig conf) {
		if (skipSync(conf))
			return;
		Set<String> names = new HashSet<>();
		if (p.isInputParameter)
			return;
		names.addAll(Formula.getVariables(p.formula));
		names.addAll(getFormulaVariables(p.uncertainty));
		writeGlobals(names, conf);
	}

	private static Set<String> getFormulaVariables(List<Parameter> params) {
		Set<String> names = new HashSet<>();
		for (Parameter param : params) {
			// no formulas in input parameters
			if (param.isInputParameter)
				continue;
			names.addAll(Formula.getVariables(param.formula));
			names.addAll(getFormulaVariables(param.uncertainty));
		}
		return names;
	}

	/**
	 * Removes the parameters from the names that are already defined in the given
	 * parameter list.
	 */
	private static void filterLocals(Set<String> names, List<Parameter> locals) {
		Set<String> localNames = locals.stream()
				.filter(p -> p.name != null)
				.map(p -> p.name.trim().toLowerCase())
				.collect(Collectors.toSet());
		names.removeIf(name -> localNames.contains(
				name.trim().toLowerCase()));
	}

	private static Set<String> getFormulaVariables(Uncertainty u) {
		Set<String> names = new HashSet<>();
		if (u == null)
			return names;
		if (u.distributionType == null)
			return names;
		if (u.distributionType == UncertaintyType.NONE)
			return names;
		names.addAll(Formula.getVariables(u.formula1));
		names.addAll(Formula.getVariables(u.formula2));
		if (u.distributionType == UncertaintyType.TRIANGLE)
			names.addAll(Formula.getVariables(u.formula3));
		return names;
	}

	private static void writeGlobals(Set<String> names, ExportConfig conf) {
		if (names.isEmpty())
			return;

		// load all global parameters
		Map<String, Parameter> globals = new ParameterDao(conf.db)
				.getGlobalParameters()
				.stream()
				.filter(p -> p.name != null)
				.collect(Collectors.toMap(
						p -> p.name.trim().toLowerCase(),
						p -> p));

		for (String name : names) {
			Parameter g = globals.get(name.trim().toLowerCase());
			if (g == null)
				continue;
			if (conf.hasVisited(ModelType.PARAMETER, g.id))
				continue;
			conf.refFn.accept(g);
			// the global parameter could have again a formula
			// that contains references to other global parameters.
			sync(g, conf);
		}
	}

	private static boolean skipSync(ExportConfig conf) {
		return conf == null
				|| !conf.exportReferences
				|| conf.refFn == null
				|| conf.db == null;
	}
}
