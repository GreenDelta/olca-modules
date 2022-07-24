package org.openlca.ipc.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.JsonObject;
import org.openlca.core.results.UpstreamNode;
import org.openlca.core.results.UpstreamTree;
import org.openlca.ipc.Responses;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;

class Upstream {

	static RpcResponse getNodesForPath(ResultRequest rr, UpstreamTree tree) {
		var path = readPathOf(rr.requestParameter());
		var array = nodesOf(tree, path)
				.stream()
				.map(node -> {
					var obj = new JsonObject();
					Json.put(obj, "@type", "UpstreamNode");
					Json.put(obj, "provider", JsonRpc.encodeTechFlow(node.provider(), rr.refs()));
					Json.put(obj, "result", node.result());
					Json.put(obj, "requiredAmount", node.requiredAmount());
					return obj;
				})
				.collect(JsonRpc.toArray());
		return Responses.ok(array, rr.request());
	}

	private static List<NodeId> readPathOf(JsonObject requestParam) {
		var array = Json.getArray(requestParam, "path");
		if (array == null || array.isEmpty())
			return List.of();
		var path = new ArrayList<NodeId>();
		for (var elem : array) {
			if (!elem.isJsonPrimitive())
				continue;
			var prim = elem.getAsJsonPrimitive();
			if (!prim.isString())
				continue;
			var segment = prim.getAsString().split("/");
			if (segment.length != 2)
				continue;
			path.add(new NodeId(segment[0], segment[1]));
		}
		return path;
	}

	private static List<UpstreamNode> nodesOf(UpstreamTree tree, List<NodeId> path) {
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

	private record NodeId(String providerId, String flowId) {

		boolean matches(UpstreamNode node) {
			if (node == null || node.provider() == null)
				return false;
			var p = node.provider();
			if (p.provider() == null || p.flow() == null)
				return false;
			return Objects.equals(providerId, p.provider().refId)
					&& Objects.equals(flowId, p.flow().refId);
		}
	}
}
