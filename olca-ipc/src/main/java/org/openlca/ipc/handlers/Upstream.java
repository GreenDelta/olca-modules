package org.openlca.ipc.handlers;

import java.util.List;
import java.util.Objects;

import org.openlca.core.results.UpstreamNode;
import org.openlca.core.results.UpstreamTree;

class Upstream {

	static List<UpstreamNode> nodesOf(UpstreamTree tree, List<NodeId> path) {
		var next = List.of(tree.root);
		for (var nodeId : path) {
			UpstreamNode node = null;
			for (var n : next) {
				if (nodeId.matches(n)) {
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

	record NodeId(String providerId, String flowId) {

		boolean matches(UpstreamNode node) {
			if (node == null || node.provider() == null)
				return false;
			var p = node.provider();
			if (p.provider() == null || p.flow() == null)
				return false;
			return Objects.equals(providerId, p.provider().refId)
					&& Objects.equals((flowId, p.flow().refId));
		}
	}

}
