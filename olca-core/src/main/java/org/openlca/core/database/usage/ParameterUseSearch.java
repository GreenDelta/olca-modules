package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.formula.Formulas;
import org.openlca.util.Strings;

/**
 * Searches for the usage of global parameters in other entities. Note that in
 * contrast to other usage searches it searches for the names of parameters in
 * formulas and parameter redefinitions.
 */
public record ParameterUseSearch(IDatabase db) implements UsageSearch {

	public Set<? extends RootDescriptor> find(String name) {
		return find(Collections.singleton(name));
	}

	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		if (ids.isEmpty())
			return Collections.emptySet();
		var names = new HashSet<String>();
		var sql = "select name from tbl_parameters where id " + Search.eqIn(ids);
		NativeSql.on(db).query(sql, r -> {
			names.add(r.getString(1));
			return true;
		});
		return find(names);
	}

	public Set<? extends RootDescriptor> find(Set<String> names) {
		if (names.isEmpty())
			return Collections.emptySet();
		var normed = names.stream()
			.filter(Objects::nonNull)
			.map(name -> name.trim().toLowerCase())
			.filter(Strings::notEmpty)
			.collect(Collectors.toSet());
		if (normed.isEmpty())
			return Collections.emptySet();

		var matcher = Matcher.create(db, normed);

		try {
			var exec = Executors.newFixedThreadPool(4);
			var results = List.of(
				exec.submit(() -> findInParameterFormulas(matcher)),
				exec.submit(() -> findInRedefs(matcher)),
				exec.submit(() -> findInExchanges(matcher)),
				exec.submit(() -> findInAllocationFactors(matcher)),
				exec.submit(() -> findInImpactFactors(matcher)));
			exec.shutdown();
			var descriptors = new HashSet<RootDescriptor>();
			for (var r : results) {
				descriptors.addAll(r.get());
			}
			return descriptors;
		} catch (Exception e) {
			throw new RuntimeException("failed to search for parameter usages", e);
		}
	}

	private Set<? extends RootDescriptor> findInParameterFormulas(Matcher matcher) {
		var q = "select id, f_owner, scope, formula from tbl_parameters " +
			"where formula is not null";

		var globalIds = new HashSet<Long>();
		var processIds = new HashSet<Long>();
		var impactIds = new HashSet<Long>();
		NativeSql.on(db).query(q, r -> {
			long owner = r.getLong(2);
			var formula = r.getString(4);

			// formulas of local parameters
			if (owner != 0) {
				if (!matcher.matchesLocalFormula(owner, formula))
					return true;
				var scope = ParameterScope.fromString(r.getString(3));
				if (scope == null)
					return true;
				if (scope == ParameterScope.PROCESS) {
					processIds.add(owner);
				} else if (scope == ParameterScope.IMPACT) {
					impactIds.add(owner);
				}
				return true;
			}

			// formulas of global parameters
			if (matcher.matchesGlobalFormula(formula)) {
				globalIds.add(r.getLong(1));
			}
			return true;
		});

		var descriptors = new HashSet<RootDescriptor>();
		if (!globalIds.isEmpty()) {
			descriptors.addAll(db.getDescriptors(Parameter.class, globalIds));
		}
		if (!processIds.isEmpty()) {
			descriptors.addAll(db.getDescriptors(Process.class, processIds));
		}
		if (!impactIds.isEmpty()) {
			descriptors.addAll(db.getDescriptors(ImpactCategory.class, impactIds));
		}
		return descriptors;
	}

	private Set<? extends RootDescriptor> findInRedefs(Matcher matcher) {
		var descriptors = new HashSet<RootDescriptor>();
		for (var system : db.getAll(ProductSystem.class)) {
			outer:
			for (var set : system.parameterSets) {
				for (var redef : set.parameters) {
					if (matcher.matchesRedef(redef)) {
						descriptors.add(Descriptor.of(system));
						break outer;
					}
				}
			}
		}

		for (var project : db.getAll(Project.class)) {
			outer:
			for (var variant : project.variants) {
				for (var redef : variant.parameterRedefs) {
					if (matcher.matchesRedef(redef)) {
						descriptors.add(Descriptor.of(project));
						break outer;
					}
				}
			}
		}

		return descriptors;
	}

	private Set<? extends RootDescriptor> findInExchanges(Matcher matcher) {
		var q = "select f_owner, resulting_amount_formula, cost_formula from " +
			"tbl_exchanges where resulting_amount_formula is not null " +
			"or cost_formula is not null";
		var ids = new HashSet<Long>();
		NativeSql.on(db).query(q, r -> {
			long owner = r.getLong(1);
			var amountFormula = r.getString(2);
			if (matcher.matchesLocalFormula(owner, amountFormula)) {
				ids.add(owner);
				return true;
			}
			var costFormula = r.getString(3);
			if (matcher.matchesLocalFormula(owner, costFormula)) {
				ids.add(owner);
			}
			return true;
		});
		return ids.isEmpty()
			? Collections.emptySet()
			: new HashSet<>(db.getDescriptors(Process.class, ids));
	}

	private Set<? extends RootDescriptor> findInAllocationFactors(Matcher matcher) {
		var q = "select f_process, formula from tbl_allocation_factors " +
			"where formula is not null";
		var ids =new HashSet<Long>();
		NativeSql.on(db).query(q, r -> {
			long id = r.getLong(1);
			var formula = r.getString(2);
			if (matcher.matchesLocalFormula(id, formula)) {
				ids.add(id);
			}
			return true;
		});
		return ids.isEmpty()
			? Collections.emptySet()
			: new HashSet<>(db.getDescriptors(Process.class, ids));
	}

	private Set<? extends RootDescriptor> findInImpactFactors(Matcher matcher) {
		var q = "select f_impact_category, formula from tbl_impact_factors " +
			"where formula is not null";
		var ids =new HashSet<Long>();
		NativeSql.on(db).query(q, r -> {
			long id = r.getLong(1);
			var formula = r.getString(2);
			if (matcher.matchesLocalFormula(id, formula)) {
				ids.add(id);
			}
			return true;
		});
		return ids.isEmpty()
			? Collections.emptySet()
			: new HashSet<>(db.getDescriptors(ImpactCategory.class, ids));
	}

	private record Matcher(Set<String> params, Map<String, TLongSet> contexts) {

		static Matcher create(IDatabase db, Set<String> params) {

			// find local definition contexts
			var q = "select name, f_owner from tbl_parameters " +
				"where f_owner is not null";
			var contexts = new HashMap<String, TLongSet>();
			NativeSql.on(db).query(q, r -> {
				var owner = r.getLong(2);
				if (owner == 0)
					return true;
				var name = norm(r.getString(1));
				if (params.contains(name)) {
					var cx = contexts.computeIfAbsent(
						name, $ -> new TLongHashSet());
					cx.add(owner);
				}
				return true;
			});
			return new Matcher(params, contexts);
		}

		boolean matchesRedef(ParameterRedef redef) {
			return redef.contextId == null && params.contains(norm(redef.name));
		}

		boolean matchesGlobalFormula(String formula) {
			for (var v : variablesOf(formula)) {
				if (params.contains(v))
					return true;
			}
			return false;
		}

		boolean matchesLocalFormula(long context, String formula) {
			if (formula == null || formula.isBlank())
				return false;
			var variables = variablesOf(formula);
			if (variables.isEmpty())
				return false;
			for (var v : variables) {
				if (!params.contains(v))
					continue;
				var cx = contexts.get(v);
				if (cx == null || !cx.contains(context))
					return true;
			}
			return false;
		}

		private Set<String> variablesOf(String formula) {
			if (formula == null || formula.isBlank())
				return Collections.emptySet();
			return Formulas.getVariables(formula)
				.stream()
				.map(Matcher::norm)
				.collect(Collectors.toSet());
		}

		static String norm(String s) {
			return s == null || s.isBlank()
				? ""
				: s.trim().toLowerCase();
		}
	}

}
