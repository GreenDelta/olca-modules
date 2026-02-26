package org.openlca.sd;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.sd.eqn.EvaluationOrder;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.SdModel;
import org.openlca.sd.xmile.Xmile;

public class CycleCheck {

	public static void main(String[] args) {
		var xmile = Xmile
			.readFrom(new File("examples/treasource-model.stmx"))
			.orElseThrow();
		var vars = SdModel.readFrom(xmile)
			.orElseThrow()
			.vars();

		var deps = new HashMap<Id, Set<Id>>();
		for (var v : vars) {
			var ds = EvaluationOrder.dependenciesOf(v);
			deps.put(v.name(), ds);
		}

		// Check for cycles in dependencies
		for (var id : deps.keySet()) {
			checkCycles(id, deps, new ArrayList<>(), new HashSet<>());
		}
	}

	private static void checkCycles(Id current, HashMap<Id, Set<Id>> deps,
			List<Id> path, Set<Id> visited) {
		if (path.contains(current)) {
			// Cycle detected
			int idx = path.indexOf(current);
			List<Id> cycle = path.subList(idx, path.size());
			System.out.println("Cycle detected: " + cycle + " -> " + current);
			return;
		}
		if (visited.contains(current)) {
			return; // already checked this branch
		}
		path.add(current);
		visited.add(current);
		Set<Id> children = deps.get(current);
		if (children != null) {
			for (Id child : children) {
				checkCycles(child, deps, path, visited);
			}
		}
		path.removeLast();
	}

}
