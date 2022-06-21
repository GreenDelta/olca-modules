package org.openlca.core.results.providers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.index.EnviIndex;

class LibUtil {

	private LibUtil() {
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
