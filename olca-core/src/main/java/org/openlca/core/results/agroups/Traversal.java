package org.openlca.core.results.agroups;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;

import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.providers.ResultProvider;

class Traversal {

	private final LcaResult result;
	private final SubGraph graph;
	private final GroupMap groups;

	private final boolean reduced;
	private final TechIndex techIdx;
	private final ResultProvider provider;

	private Traversal(
			LcaResult result, GroupMap groups, SubGraph graph
	) {
		this.result = result;
		this.groups = groups;
		this.graph = graph;
		this.reduced = graph.linkCount() > 250;
		this.techIdx = result.techIndex();
		this.provider = result.provider();
	}

	static Tree treeOf(LcaResult result, GroupMap groups, SubGraph graph) {
		return new Traversal(result, groups, graph).build();
	}

	private Tree build() {

		var root = Tree.rootOf(result, groups);
		var queue = new ArrayDeque<TravNode>();
		queue.add(new TravNode(null, root));
		var visited = new HashSet<Long>();
		visited.add(root.pid());

		while (!queue.isEmpty()) {
			var parent = queue.poll();
			var links = graph.linksOf(parent.pid());
			if (links.isEmpty())
				continue;

			for (var link : links) {
				long pid = link.providerId;
				boolean vis = visited.contains(pid);

				// check for a cycle in the path
				if (vis && parent.hasPidInPath(pid)) {
					continue;
				}

				boolean add;
				boolean follow;
				if (!reduced) {
					add = true;
					follow = true;
				} else {
					add = groups.isGrouped(pid);
					follow = !vis;
				}

				if (!add)
					continue;
				var child = childOf(parent, link);
				if (child == null)
					continue;
				if (follow) {
					visited.add(pid);
					queue.add(child);
				}
			}
		}

		return root;
	}

	private TravNode childOf(TravNode parent, ProcessLink link) {
		var techFlow = techIdx.getProvider(link.providerId, link.flowId);
		if (techFlow == null)
			return null;
		int index = techIdx.of(techFlow);
		double aji = provider.techValueOf(index, parent.tree().index());
		double ajj = provider.techValueOf(index, index);
		double sj = parent.tree().scaling() * (-aji / ajj);
		var group = groups.map().getOrDefault(
				techFlow.providerId(), parent.tree().group());
		var child = new Tree(
				group, techFlow, index, sj, sj * ajj, new ArrayList<>()
		);
		parent.tree.childs().add(child);
		return new TravNode(parent, child);
	}

	private record TravNode(TravNode parent, Tree tree) {

		long pid() {
			return tree.pid();
		}

		boolean hasPidInPath(long pid) {
			return pid == tree.pid()
					|| (parent != null && parent.hasPidInPath(pid));
		}
	}
}
