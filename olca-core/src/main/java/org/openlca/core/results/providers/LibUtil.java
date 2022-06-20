package org.openlca.core.results.providers;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.index.EnviIndex;
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
				var lib = dir.getLibrary(libID);
				if (lib.isEmpty())
					continue;
				var next = lib.get()
					.syncTechIndex(db)
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
	 * Load the flow indices for the given libraries.
	 */
	static Map<String, EnviIndex> loadFlowIndicesOf(
		Set<String> libraries, LibraryDir dir, IDatabase db) {
		var map = new HashMap<String, EnviIndex>(libraries.size());
		for (var libID : libraries) {
			var lib = dir.getLibrary(libID).orElse(null);
			if (lib == null)
				continue;
			var flowIdx = lib.syncEnviIndex(db)
				.orElse(null);
			map.put(libID, flowIdx);
		}
		return map;
	}

	/**
	 * Creates the combined flow index of the given flow index of
	 * the foreground system and the flow indices of the libraries.
	 */
	static EnviIndex combinedFlowIndexOf(
		EnviIndex index, Collection<EnviIndex> libIndices) {

		EnviIndex fullIndex;
		if (index != null) {
			fullIndex = index.isRegionalized()
				? EnviIndex.createRegionalized()
				: EnviIndex.create();
			fullIndex.addAll(index);
		} else {

			// the flow index of the foreground system could be
			// null, but there could be still flow indices in the
			// libraries. if there is at least one regionalized
			// library we create a regionalized index in this case
			var regionalized = libIndices.stream()
				.anyMatch(EnviIndex::isRegionalized);
			fullIndex = regionalized
				? EnviIndex.createRegionalized()
				: EnviIndex.create();
		}

		libIndices.forEach(fullIndex::addAll);

		return fullIndex;
	}

}
