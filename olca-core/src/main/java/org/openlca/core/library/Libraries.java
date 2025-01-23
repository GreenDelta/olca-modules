package org.openlca.core.library;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.reader.LibReader;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.Descriptor;

public final class Libraries {

	private Libraries() {
	}

	/**
	 * Returns the dependencies of the given library in topological order. The
	 * returned list contains the given library itself at the last position as
	 * no cycles are allowed in a library dependency graph.
	 */
	public static List<Library> dependencyOrderOf(Library lib) {
		if (lib == null)
			return Collections.emptyList();

		var stack = new Stack<Library>();
		var queue = new ArrayDeque<Library>();
		queue.add(lib);
		while (!queue.isEmpty()) {
			var next = queue.poll();
			stack.push(next);
			queue.addAll(next.getDirectDependencies());
		}

		var handled = new HashSet<Library>();
		var order = new ArrayList<Library>();
		while (!stack.isEmpty()) {
			var next = stack.pop();
			if (!handled.contains(next)) {
				order.add(next);
				handled.add(next);
			}
		}
		return order;
	}

	public static void fillExchangesOf(IDatabase db, LibReader lib, Process process) {
		var exchanges = lib.getExchanges(TechFlow.of(process), db);
		var qref = process.quantitativeReference;
		if (qref != null) {
			process.quantitativeReference = exchanges.stream()
					.filter(e -> Objects.equals(qref.flow, e.flow)
							& qref.isInput == e.isInput)
					.findFirst()
					.orElse(null);
		}
		process.exchanges.clear();
		process.exchanges.addAll(exchanges);
	}

	public static void fillFactorsOf(IDatabase db, LibReader lib, ImpactCategory impact) {
		var factors = lib.getImpactFactors(Descriptor.of(impact), db);
		impact.impactFactors.addAll(factors);
	}

}
