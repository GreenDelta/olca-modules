package org.openlca.jsonld.output;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.util.Formula;

public class ParameterReferences {

	public static void writeReferencedParameters(Project p, ExportConfig conf) {
		if (!conf.exportReferences)
			return;
		Set<String> names = new HashSet<>();
		for (ProjectVariant v : p.variants)
			names.addAll(getRedefVariables(v.parameterRedefs));
		writeParameters(names, conf);
	}

	public static void writeReferencedParameters(ProductSystem s,
			ExportConfig conf) {
		if (!conf.exportReferences)
			return;
		Set<String> names = getRedefVariables(s.parameterRedefs);
		writeParameters(names, conf);
	}

	private static Set<String> getRedefVariables(List<ParameterRedef> redefs) {
		Set<String> names = new HashSet<>();
		for (ParameterRedef redef : redefs)
			if (redef.contextType == null)
				names.add(redef.name);
		return names;
	}

	public static void writeReferencedParameters(Process p, ExportConfig conf) {
		if (!conf.exportReferences)
			return;
		Set<String> names = new HashSet<>();
		for (Exchange e : p.exchanges) {
			names.addAll(Formula.getVariables(e.amountFormula));
			names.addAll(Formula.getVariables(e.costFormula));
			names.addAll(getUncercaintyVariables(e.uncertainty));
		}
		names.addAll(getParameterVariables(p.parameters));
		filterLocal(names, p.parameters);
		writeParameters(names, conf);
	}

	private static Set<String> getParameterVariables(List<Parameter> parameters) {
		Set<String> names = new HashSet<>();
		for (Parameter param : parameters) {
			// no formulas in input parameters
			if (param.isInputParameter)
				continue;
			names.addAll(Formula.getVariables(param.formula));
			names.addAll(getUncercaintyVariables(param.uncertainty));
		}
		return names;
	}



	public static void writeReferencedParameters(ImpactMethod m,
			ExportConfig conf) {
		if (!conf.exportReferences)
			return;
		Set<String> names = new HashSet<>();
		for (ImpactCategory c : m.impactCategories) {
			for (ImpactFactor f : c.impactFactors) {
				names.addAll(Formula.getVariables(f.formula));
				names.addAll(getUncercaintyVariables(f.uncertainty));
			}
		}
		names.addAll(getParameterVariables(m.parameters));
		filterLocal(names, m.parameters);
		writeParameters(names, conf);
	}

	/**
	 * Removes the parameters from the names that are already defined in the
	 * given parameter list.
	 */
	private static void filterLocal(Set<String> names,
			List<Parameter> parameters) {
		HashSet<String> removals = new HashSet<>();
		for (String name : new HashSet<>(names)) {
			for (Parameter param : parameters) {
				if (param.name == null)
					continue;
				if (param.name.equalsIgnoreCase(name)) {
					removals.add(name);
					break;
				}
			}
		}
		names.removeAll(removals);
	}

	private static Set<String> getUncercaintyVariables(Uncertainty u) {
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

	public static void writeReferencedParameters(Parameter p, ExportConfig conf) {
		if (conf.db == null || conf.refFn == null)
			return;
		Set<String> names = new HashSet<>();
		if (p.isInputParameter)
			return;
		names.addAll(Formula.getVariables(p.formula));
		names.addAll(getUncercaintyVariables(p.uncertainty));
		writeParameters(names, conf);
	}

	private static void writeParameters(Set<String> names, ExportConfig conf) {
		if (names.isEmpty() || conf.db == null)
			return;
		ParameterDao dao = new ParameterDao(conf.db);
		for (String name : names) {
			Parameter p = loadParameter(name, dao);
			if (p == null || conf.refFn == null)
				continue;
			if (conf.hasVisited(ModelType.PARAMETER, p.id))
				continue;
			conf.refFn.accept(p);
			writeReferencedParameters(p, conf);
		}
	}

	private static Parameter loadParameter(String name, ParameterDao dao) {
		String jpql = "SELECT p FROM Parameter p WHERE "
				+ "p.scope = :scope AND LOWER(p.name) = :name";
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("name", name);
		parameters.put("scope", ParameterScope.GLOBAL);
		return dao.getFirst(jpql, parameters);
	}
}
