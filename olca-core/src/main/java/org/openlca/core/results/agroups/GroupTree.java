package org.openlca.core.results.agroups;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.LcaResult;

class GroupTree {

	private final ProductSystem system;
	private final GroupMap groups;
	private final LcaResult result;

	GroupTree(ProductSystem system, GroupMap groups, LcaResult result) {
		this.system = system;
		this.groups = groups;
		this.result = result;
	}

	PathNode build() {
		var subLinks = collectSubLinks();

		var root = Node.rootOf(result, groups);
		var tree = PathNode.rootOf(root);
		var provider = result.provider();
		var techIdx = result.techIndex();
		var queue = new ArrayDeque<PathNode>();
		queue.add(tree);

		while(!queue.isEmpty()) {
			var parent = queue.poll();
			var links = subLinks.get(parent.providerId());
			if (links == null)
				continue;

			for (var link : links) {
				var techFlow = techIdx.getProvider(link.providerId, link.flowId);
				if (techFlow == null || parent.contains(techFlow))
					continue;
				int index = techIdx.of(techFlow);
				double aji = provider.techValueOf(index, parent.node().index());
				double ajj = provider.techValueOf(index, index);
				double sj = parent.node().scaling() * (-aji / ajj);
				var group = groups.map().getOrDefault(
					techFlow.providerId(), parent.node().group());
				var node = new Node(group, techFlow, index, sj, sj * ajj);
				var next = parent.add(node);
				queue.add(next);
			}
		}

		return tree;
	}

	private Map<Long, List<ProcessLink>> collectSubLinks() {

		// index back links, from providers to the processes
		var linkIdx = new HashMap<Long, List<ProcessLink>>();
		for (var link : system.processLinks) {
			linkIdx.computeIfAbsent(link.providerId, $ -> new ArrayList<>())
				.add(link);
		}

		// collect the back links of the group nodes recursively
		var subLinks = new ArrayList<ProcessLink>();
		var visited = new HashSet<Long>();
		var queue = new ArrayDeque<>(groups.map().keySet());
		while (!queue.isEmpty()) {
			var next = queue.poll();
			if (visited.contains(next))
				continue;
			visited.add(next);
			var links = linkIdx.get(next);
			if (links == null)
				continue;
			subLinks.addAll(links);
			for (var link : links) {
				var p = link.processId;
				if (!visited.contains(p) && !queue.contains(p)) {
					queue.add(p);
				}
			}
		}

		// index forward links, from processes to providers
		linkIdx.clear();
		for (var link : subLinks) {
			linkIdx.computeIfAbsent(link.processId, $ -> new ArrayList<>())
				.add(link);
		}
		return linkIdx;
	}

}
