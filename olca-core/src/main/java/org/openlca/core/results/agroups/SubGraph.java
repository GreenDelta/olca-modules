package org.openlca.core.results.agroups;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The sub-graph of the product system which when expanded to a tree would
/// have the reference process as root and the grouped nodes as leafs of that
/// tree. Multiple nodes with different groups could occur along a path in such
/// a tree but the final node would be also a grouped node. It can be very
/// memory intensive when such a tree is really constructed, and thus it is
/// better to collect the results in a traversal of that sub-graph.
class SubGraph {

	private final HashMap<Long, List<ProcessLink>> links = new HashMap<>();
	private final HashSet<Long> nodes = new HashSet<>();

	private SubGraph() {
	}

	static SubGraph of(ProductSystem system, GroupMap groups) {
		return new Builder(system, groups).build();
	}

	boolean isEmpty() {
		return nodes.isEmpty() || links.isEmpty();
	}

	List<ProcessLink> linksOf(long pid) {
		var list = links.get(pid);
		return list != null ? list : Collections.emptyList();
	}

	private static class Builder {

		final Logger log = LoggerFactory.getLogger(getClass());
		final ProductSystem system;
		final GroupMap groups;
		final SubGraph g;

		private Builder(ProductSystem system, GroupMap groups) {
			this.system = system;
			this.groups = groups;
			g = new SubGraph();
		}

		SubGraph build() {
			if (system == null
				|| system.referenceProcess == null
				|| groups == null
				|| groups.isEmpty())
				return g;

			log.info("build sub-graph for analysis groups");
			log.trace("build link index");
			var linkIdx = new HashMap<Long, List<ProcessLink>>();
			for (var link : system.processLinks) {
				var list = linkIdx.computeIfAbsent(
					link.processId, $ -> new ArrayList<>());
				list.add(link);
			}

			log.trace("collect nodes for sub-graph");
			var visited = new HashSet<Long>();
			var queue = new ArrayDeque<Node>();
			long refId = system.referenceProcess.id;
			queue.add(new Node(refId, null));
			visited.add(refId);

			while (!queue.isEmpty()) {
				var parent = queue.poll();
				var links = linkIdx.get(parent.id);
				if (links == null)
					continue;
				for (var link : links) {
					long nextId = link.providerId;
					if (groups.isGrouped(nextId)) {
						g.nodes.add(nextId);
						parent.addPathTo(g);
					}
					if (visited.contains(nextId))
						continue;
					visited.add(nextId);
					queue.add(new Node(nextId, parent));
				}
			}

			log.trace("filter links");
			int linkCount = 0;
			for (var n : g.nodes) {
				var links = linkIdx.get(n);
				if (links == null)
					continue;
				for (var link : links) {
					if (g.nodes.contains(link.providerId)) {
						linkCount++;
						g.links.computeIfAbsent(n, $ -> new ArrayList<>()).add(link);
					}
				}
			}

			log.info("created a sub-graph with {} nodes and {} links",
				g.nodes.size(), linkCount);
			return g;
		}

		record Node(long id, Node prev) {

			void addPathTo(SubGraph g) {
				g.nodes.add(id);
				if (prev != null) {
					prev.addPathTo(g);
				}
			}
		}
	}
}
