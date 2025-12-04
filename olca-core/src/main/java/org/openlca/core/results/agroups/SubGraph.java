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

/// A sub-graph of a product system containing all paths from the reference
/// process to providers that are assigned to analysis groups. The sub-graph
/// includes:
///
/// - all grouped providers (as leaf nodes when viewed as a tree)
/// - all intermediate processes along paths to grouped providers
/// - links connecting these nodes where both endpoints are in the sub-graph
///
/// Duplicate links (same process, flow, and provider) are filtered out during
/// construction. Instead of expanding the full tree (which can be very memory
/// intensive), results should be collected by traversing this sub-graph.
record SubGraph(
		HashSet<Long> nodes, HashMap<Long, List<ProcessLink>> links, int linkCount
) {

	static SubGraph of(ProductSystem system, GroupMap groups) {
		return new Builder(system, groups).build();
	}

	List<ProcessLink> linksOf(long pid) {
		var list = links.get(pid);
		return list != null ? list : Collections.emptyList();
	}

	private static class Builder {

		final Logger log = LoggerFactory.getLogger(getClass());
		final ProductSystem system;
		final GroupMap groups;

		final HashSet<Long> nodes = new HashSet<>();
		final HashMap<Long, List<ProcessLink>> links = new HashMap<>();

		private Builder(ProductSystem system, GroupMap groups) {
			this.system = system;
			this.groups = groups;
		}

		SubGraph build() {
			if (system == null
				|| system.referenceProcess == null
				|| groups == null
				|| groups.isEmpty())
				return new SubGraph(nodes, links, 0);

			log.info("build sub-graph for analysis groups");
			log.trace("build link index");
			var linkIdx = new HashMap<Long, List<ProcessLink>>();
			var handled = new HashSet<LinkId>();
			for (var link : system.processLinks) {
				// remove double links
				var id = new LinkId(link.processId, link.flowId, link.providerId);
				if (!handled.add(id))
					continue;
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
						// if it is a grouped node, add the node and
						// the path to the node to the collected nodes
						nodes.add(nextId);
						parent.addPathTo(this);
					}
					// nodes can be visited via different paths
					if (visited.contains(nextId))
						continue;
					visited.add(nextId);
					queue.add(new Node(nextId, parent));
				}
			}

			// we only add links to the sub-graph where the process
			// and provider are nodes of that sub-graph
			log.trace("filter links");
			int linkCount = 0;
			for (var n : nodes) {
				var nextLinks = linkIdx.get(n);
				if (nextLinks == null)
					continue;
				for (var link : nextLinks) {
					if (nodes.contains(link.providerId)) {
						linkCount++;
						links.computeIfAbsent(n, $ -> new ArrayList<>()).add(link);
					}
				}
			}

			log.info("created a sub-graph with {} nodes and {} links",
				nodes.size(), linkCount);
			return new SubGraph(nodes, links, linkCount);
		}

		private record Node(long id, Node prev) {

			/// With this node as the leaf of a path, this method adds
			/// all nodes along the path to the collected nodes.
			void addPathTo(Builder b) {
				for (var n = this; n != null; n = n.prev) {
					b.nodes.add(n.id);
				}
			}
		}

		private record LinkId(long processId, long flowId, long providerId) {
		}
	}
}
