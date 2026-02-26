package org.openlca.sd.eqn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.commons.Res;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.model.cells.BoolCell;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.EmptyCell;
import org.openlca.sd.model.cells.EqnCell;
import org.openlca.sd.model.cells.LookupCell;
import org.openlca.sd.model.cells.LookupEqnCell;
import org.openlca.sd.model.cells.NonNegativeCell;
import org.openlca.sd.model.cells.NumCell;
import org.openlca.sd.model.cells.TensorCell;
import org.openlca.sd.model.cells.TensorEqnCell;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.LookupFunc;
import org.openlca.sd.model.Var;

public class EvaluationOrder {

	public static Res<List<Var>> of(Collection<Var> vars) {
		return vars == null || vars.isEmpty()
			? Res.error("no variables provided")
			: new Sorter(vars).sort();
	}

	/// Returns the evaluation dependencies of the given var. The variables
	/// with the given IDs must be evaluated before the given variable.
	public static Set<Id> dependenciesOf(Var var) {
		if (var == null)
			return Set.of();
		var ids = new HashSet<Id>();
		fillDeps(var.def(), ids);
		return ids;
	}

	private static void fillDeps(Cell cell, Set<Id> ids) {
		switch (cell) {

			case EqnCell(String eqn) -> {
				var vars = Interpreter.varsOf(eqn);
				if (!vars.isError()) {
					ids.addAll(vars.value());
				}
			}

			case LookupEqnCell(String eqn, LookupFunc ignored) -> {
				var vars = Interpreter.varsOf(eqn);
				if (!vars.isError()) {
					ids.addAll(vars.value());
				}
			}

			case TensorCell(Tensor tensor) -> {
				for (int i = 0; i < tensor.size(); i++) {
					fillDeps(tensor.get(i), ids);
				}
			}

			case TensorEqnCell(Cell eqn, Tensor tensor) -> {
				fillDeps(eqn, ids);
				for (int i = 0; i < tensor.size(); i++) {
					fillDeps(tensor.get(i), ids);
				}
			}

			case NonNegativeCell(Cell inner) -> fillDeps(inner, ids);

			case BoolCell(boolean ignore) -> {
			}
			case NumCell(double ignore) -> {
			}
			case LookupCell(LookupFunc ignore) -> {
			}
			case EmptyCell() -> {
			}
		}
	}

	private static class Sorter {

		private final Map<Id, Var> vars = new HashMap<>();
		private final Set<Id> unordered = new HashSet<>();
		private final List<Id> ordered = new ArrayList<>();
		private final Map<Id, Integer> inDegrees = new HashMap<>();
		private final Map<Id, Set<Id>> successors = new HashMap<>();

		Sorter(Collection<Var> vars) {
			for (var v : vars) {
				this.vars.put(v.name(), v);
				unordered.add(v.name());
				var deps = dependenciesOf(v);
				inDegrees.put(v.name(), deps.size());
				for (var dep : deps) {
					successors.computeIfAbsent(dep, $ -> new HashSet<>())
						.add(v.name());
				}
			}
		}

		Res<List<Var>> sort() {

			while (!unordered.isEmpty()) {

				Id next = null;
				for (var indeg : inDegrees.entrySet()) {
					if (indeg.getValue() == 0) {
						next = indeg.getKey();
					}
				}

				if (next == null)
					return Res.error("there are cycles in the evaluation graph");

				ordered.add(next);
				unordered.remove(next);
				inDegrees.remove(next);
				var ss = successors.remove(next);
				if (ss != null) {
					for (var suc : ss) {
						inDegrees.compute(
							suc, ($, num) -> num == null || num == 0 ? 0 : num - 1);
					}
				}
			}

			var sorted = new ArrayList<Var>(ordered.size());
			for (var id : ordered) {
				sorted.add(vars.get(id));
			}
			return Res.ok(sorted);
		}
	}
}
