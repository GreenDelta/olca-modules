package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.openlca.core.database.ParameterDao;
import org.openlca.core.matrix.ProductSystemBuilder;
import org.openlca.core.matrix.index.LongPair;
import org.openlca.core.matrix.linking.SubSystemLinker;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.Process;
import org.openlca.simapro.csv.process.ProductStageBlock;
import org.openlca.simapro.csv.process.TechExchangeRow;
import org.openlca.util.Pair;
import org.openlca.util.Strings;
import org.openlca.util.TopoSort;

record ProductSystems(ImportContext context) {

	static void map(
		ImportContext context, List<Pair<ProductStageBlock, Process>> pairs) {
		if (pairs.isEmpty())
			return;
		var entries = pairs.stream()
			.map(pair -> new Entry(pair.first, pair.second))
			.collect(Collectors.toList());
		new ProductSystems(context).map(entries);
	}

	private void map(List<Entry> entries) {

		// Life cycles can include other life cycles which
		// need to be created first. Thus, we need to sort
		// the life cycles in topological order first.
		var sorted = sortInDependencyOrder(entries);

		// create the product systems
		for (var e : sorted) {
			var linker = new SubSystemLinker(context.db());
			var system = new ProductSystemBuilder(linker).build(e.process);
			var params = wasteScenarioParametersOf(e.block);
			if (params != null) {
				system.parameterSets.add(params);
			}
			context.insert(system);
		}
	}

	private ParameterRedefSet wasteScenarioParametersOf(ProductStageBlock block) {
		var ws = block.wasteOrDisposalScenario();
		if (ws == null)
			return null;
		var param = WasteScenarios.parameterOf(ws.name());
		var paramSet = new ParameterRedefSet();
		paramSet.name = "Parameters";
		paramSet.isBaseline = true;

		for (var global : new ParameterDao(context.db()).getGlobalParameters()) {
			if (!global.isInputParameter
				|| global.name == null
				|| !global.name.startsWith(WasteScenarios.PARAMETER_PREFIX)
				|| global.value != 0)
				continue;

			var redef = ParameterRedef.of(global);
			redef.isProtected = true;
			redef.value = Strings.nullOrEqual(param, redef.name)
				? 1.0
				: 0.0;
			paramSet.parameters.add(redef);
		}
		return paramSet.parameters.isEmpty()
			? null
			: paramSet;
	}

	private List<Entry> sortInDependencyOrder(List<Entry> entries) {

		// collect the dependencies
		var index = new HashMap<String, Long>();
		for (var e : entries) {
			index.put(e.name(), e.id());
		}
		var deps = new ArrayList<LongPair>();
		for (var e : entries) {
			for (var depName : e.dependencies()) {
				var dep = index.get(depName);
				if (dep != null) {
					deps.add(LongPair.of(dep, e.id()));
				}
			}
		}

		// sort the dependencies; return the unchanged list when
		// there is no dependency order
		if (deps.isEmpty())
			return entries;
		var topoOrder = TopoSort.of(deps);
		if (topoOrder == null)
			return entries;

		// apply the topological order
		var handled = new HashSet<Long>();
		var sorted = new ArrayList<Entry>();
		for (var id : topoOrder) {
			handled.add(id);
			for (var e : entries) {
				if (e.id() == id) {
					sorted.add(e);
					break;
				}
			}
		}

		// add the life cycles with no dependencies or dependents
		// to the end of the list
		for (var e : entries) {
			if (!handled.contains(e.id())) {
				sorted.add(e);
			}
		}

		return sorted;
	}


	private record Entry(ProductStageBlock block, Process process) {

		long id() {
			return process.id;
		}

		String name() {
			var products = block.products();
			return products.isEmpty()
				? ""
				: Strings.orEmpty(products.get(0).name());
		}

		List<String> dependencies() {
			return block.additionalLifeCycles()
				.stream()
				.map(TechExchangeRow::name)
				.collect(Collectors.toList());
		}
	}
}
