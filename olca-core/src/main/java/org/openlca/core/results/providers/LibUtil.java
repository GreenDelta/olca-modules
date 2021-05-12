package org.openlca.core.results.providers;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.index.FlowIndex;
import org.openlca.core.matrix.index.TechIndex;

class LibUtil {

	private LibUtil() {
	}

	/**
	 * Recursively loads the tech-indices of the used libraries
	 * starting from the given tech-index of the foreground system.
	 */
	static Map<String, TechIndex> loadTechIndicesOf(
		TechIndex index, LibraryDir dir, IDatabase db) {

		var map = new HashMap<String, TechIndex>();
		var queue = new ArrayDeque<TechIndex>();
		queue.add(index);
		while (!queue.isEmpty()) {
			var idx = queue.poll();

			// collect the next non-handled libraries
			var nextLibs = new HashSet<String>();
			idx.each((_i, product) -> {
				var lib = product.library();
				if (lib == null
					|| nextLibs.contains(lib)
					|| map.containsKey(lib))
					return;
				nextLibs.add(lib);
			});

			// load the tech-indices from these libraries
			for (var libID : nextLibs) {
				var lib = dir.get(libID);
				if (lib.isEmpty())
					continue;
				var next = lib.get()
					.syncProducts(db)
					.orElse(null);
				if (next != null) {
					map.put(libID, next);
					queue.add(next);
				}
			}
		}

		return map;
	}

	/**
	 * Creates the combined tech. index from the given tech-index
	 * of the foreground system and the indices of the libraries.
	 */
	static TechIndex combinedTechIndexOf(
		TechIndex index, Collection<TechIndex> libIndices) {
		var fullIdx = new TechIndex(index.getRefFlow());
		fullIdx.setDemand(index.getDemand());
		fullIdx.addAll(index);
		libIndices.forEach(fullIdx::addAll);
		return fullIdx;
	}

	/**
	 * Load the flow indices for the given libraries.
	 */
	static Map<String, FlowIndex> loadFlowIndicesOf(
		Set<String> libraries, LibraryDir dir, IDatabase db) {
		var map = new HashMap<String, FlowIndex>(libraries.size());
		for (var libID : libraries) {
			var lib = dir.get(libID).orElse(null);
			if (lib == null)
				continue;
			var flowIdx = lib.syncElementaryFlows(db)
				.orElse(null);
			map.put(libID, flowIdx);
		}
		return map;
	}

	/**
	 * Creates the combined flow index of the given flow index of
	 * the foreground system and the flow indices of the libraries.
	 */
	static FlowIndex combinedFlowIndexOf(
		FlowIndex index, Collection<FlowIndex> libIndices) {

		FlowIndex fullIndex;
		if (index != null) {
			fullIndex = index.isRegionalized()
				? FlowIndex.createRegionalized()
				: FlowIndex.create();
			fullIndex.addAll(index);
		} else {

			// the flow index of the foreground system could be
			// null, but there could be still flow indices in the
			// libraries. if there is at least one regionalized
			// library we create a regionalized index in this case
			var regionalized = libIndices.stream()
				.anyMatch(FlowIndex::isRegionalized);
			fullIndex = regionalized
				? FlowIndex.createRegionalized()
				: FlowIndex.create();
		}

		libIndices.forEach(fullIndex::addAll);

		return fullIndex;
	}

}
