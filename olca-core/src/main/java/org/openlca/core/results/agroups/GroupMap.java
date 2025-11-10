package org.openlca.core.results.agroups;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.AnalysisGroup;
import org.openlca.core.results.LcaResult;
import org.openlca.commons.Strings;

record GroupMap(String top, Map<Long, String> map) {

	static GroupMap of(LcaResult result, List<AnalysisGroup> groups) {

		if (result == null || groups == null || groups.isEmpty())
			return new GroupMap("Top", Collections.emptyMap());

		// invert the group mapping
		var names = new HashSet<String>();
		var map = new HashMap<Long, String>();
		for (var g : groups) {
			if (Strings.isBlank(g.name) || g.processes.isEmpty())
				continue;
			var name = g.name;
			names.add(name);
			for (var p : g.processes) {
				map.put(p, name);
			}
		}

		// check if there is a "Top" group defined
		var root = result.demand() != null
				? result.demand().techFlow()
				: null;
		if (root != null) {
			var top = map.get(root.providerId());
			if (top != null)
				return new GroupMap(top, map);
		}

		// make sure that the special group "Top" is unique
		var tops = List.of("Top", "Base", "Root");
		for (var top : tops) {
			if (!names.contains(top))
				return new GroupMap(top, map);
		}
		var top = "_Top_";
		while (names.contains(top)) {
			top = "_" + top + "_";
		}
		return new GroupMap(top, map);
	}

	boolean isEmpty() {
		return map.isEmpty();
	}

	boolean isGrouped(long pid) {
		return map.get(pid) != null;
	}
}
