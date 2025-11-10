package org.openlca.core.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.results.UpstreamNode;
import org.openlca.core.results.UpstreamTree;
import org.openlca.commons.Strings;

/**
 * A utility class for identifying nodes in an upstream tree. The path to a
 * node in an upstream tree is unique for a sequence of technosphere flows.
 * Such a path is encoded using the IDs of the provider-flow pairs in a single
 * string where the flow separator is {@code "::"} and the node separator
 * {@code "/"}, e.g.:
 * {@code "<provider1-id>::<flow1-id>/<provider2-id>::<flow2-id>/..."}
 */
class UpstreamPath {

	private final List<TechFlowId> segments;

	private UpstreamPath(List<TechFlowId> segments) {
		this.segments = segments;
	}

	static UpstreamPath parse(String path) {
		if (Strings.isBlank(path))
			return new UpstreamPath(Collections.emptyList());
		var ids = new ArrayList<TechFlowId>();
		for (var node : path.split("/")) {
			ids.add(TechFlowId.parse(node));
		}
		return new UpstreamPath(ids);
	}

	List<UpstreamNode> selectChilds(UpstreamTree tree) {
		if (tree == null)
			return Collections.emptyList();
		if (segments.isEmpty())
			return List.of(tree.root);
		var next = List.of(tree.root);
		for (var nodeId : segments) {
			UpstreamNode node = null;
			for (var n : next) {
				if (nodeId.matches(n.provider())) {
					node = n;
					break;
				}
			}
			if (node == null) {
				next = List.of();
				break;
			}
			next = tree.childs(node);
		}
		return next;
	}
}
