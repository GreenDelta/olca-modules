package org.openlca.io.ilcd.output;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Process;
import org.openlca.formula.Formulas;
import org.openlca.ilcd.processes.Parameter;
import org.openlca.ilcd.util.ParameterExtension;
import org.openlca.io.ilcd.Ext;
import org.openlca.commons.Strings;

class ProcessParameters {

	private final Export exp;
	private final Process process;

	private ProcessParameters(Export exp, Process process) {
		this.exp = exp;
		this.process = process;
	}

	static List<Parameter> convert(
			Export exp, Process process
	) {
		return new ProcessParameters(exp, process).convert(process);
	}

	private List<Parameter> convert(Process process) {

		// convert the process parameters
		var params = processParams(process);

		// collect the names of used global parameters
		// that are not defined as local parameter
		var locals = new HashSet<String>();
		for (var param : params) {
			locals.add(keyOf(param.getName()));
		}
		var usedGlobals = getUsedGlobals(locals);

		// add global parameters if required
		if (!usedGlobals.isEmpty()) {
			addGlobals(params, usedGlobals);
		}
		return params;
	}

	private void addGlobals(List<Parameter> params, Set<String> used) {

		// index the global parameters
		var globals = new HashMap<String, org.openlca.core.model.Parameter>();
		for (var global : new ParameterDao(exp.db).getGlobalParameters()) {
			globals.put(keyOf(global.name), global);
		}

		// add the used global parameters, also from formulas of them!
		var queue = new ArrayDeque<>(used);
		var handled = new HashSet<String>();
		while (!queue.isEmpty()) {
			var next = queue.poll();
			handled.add(next);
			var global = globals.get(next);
			if (global == null)
				continue;
			params.add(convert(global, "global"));
			for (var v : Formulas.getVariables(global.formula)) {
				var key = keyOf(v);
				if (!handled.contains(key) && !queue.contains(key)) {
					queue.add(key);
				}
			}
		}
	}

	private List<Parameter> processParams(Process process) {
		var iParams = new ArrayList<Parameter>();
		for (var oParam : process.parameters) {
			if (Strings.isBlank(oParam.name))
				continue;
			var iParam = convert(oParam, "process");
			iParams.add(iParam);
		}
		return iParams;
	}

	private Parameter convert(
			org.openlca.core.model.Parameter o, String scope
	) {
		var i = new Parameter()
				.withName(o.name)
				.withFormula(o.formula)
				.withMean(o.value);
		Ext.setUUID(i, o.refId);
		new UncertaintyConverter().map(o, i);
		exp.add(i::withComment, o.description);
		new ParameterExtension(i).setScope(scope);
		return i;
	}

	/// Get the used global parameters from the formulas in the process,
	/// that are not defined as local parameter.
	private Set<String> getUsedGlobals(Set<String> locals) {
		if (process == null)
			return Set.of();
		var globals = new HashSet<String>();
		Consumer<String> check = formula -> {
			for (var v : Formulas.getVariables(formula)) {
				var key = keyOf(v);
				if (!locals.contains(key)) {
					globals.add(key);
				}
			}
		};

		for (var e : process.exchanges) {
			check.accept(e.formula);
			check.accept(e.costFormula);
		}
		for (var p : process.parameters) {
			check.accept(p.formula);
		}
		for (var f : process.allocationFactors) {
			check.accept(f.formula);
		}
		return globals;
	}

	private String keyOf(String name) {
		return name != null
				? name.strip().toLowerCase()
				: "";
	}

}
