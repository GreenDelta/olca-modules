package org.openlca.core.services;

import org.openlca.core.results.Sankey;
import org.openlca.core.results.Sankey.Node;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.output.JsonRefs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gnu.trove.impl.Constants;
import gnu.trove.set.hash.TIntHashSet;

class JsonSankeyGraph {

	private final Sankey<?> sankey;
	private final JsonRefs refs;

	private final TIntHashSet handledNodes = new TIntHashSet(
			Constants.DEFAULT_CAPACITY,
			Constants.DEFAULT_LOAD_FACTOR,
			-1
	);
	private final JsonArray nodes = new JsonArray();
	private final JsonArray edges = new JsonArray();

	private JsonSankeyGraph(Sankey<?> sankey, JsonRefs refs) {
		this.sankey = sankey;
		this.refs = refs;
	}

	static JsonObject of(Sankey<?> sankey, JsonRefs refs) {
		return new JsonSankeyGraph(sankey, refs).convert();
	}

	private JsonObject convert() {
		var obj = new JsonObject();
		Json.put(obj, "rootIndex", sankey.root.index);
		sankey.traverse(node -> {
			pushNode(node);
			for (var provider : node.providers) {
				pushNode(provider);
				pushEdge(provider, node);
			}
		});
		Json.put(obj, "nodes", nodes);
		Json.put(obj, "edges", edges);
		return obj;
	}

	private void pushNode(Node node) {
		if (handledNodes.contains(node.index))
			return;
		handledNodes.add(node.index);
		var obj = new JsonObject();
		Json.put(obj, "index",
				node.index);
		Json.put(obj, "techFlow",
				JsonUtil.encodeTechFlow(node.product, refs));
		Json.put(obj, "directResult",
				node.direct);
		Json.put(obj, "totalResult",
				node.total);
		nodes.add(obj);
	}

	private void pushEdge(Node provider, Node node) {
		var obj = new JsonObject();
		Json.put(obj, "nodeIndex",
				node.index);
		Json.put(obj, "providerIndex",
				provider.index);
		Json.put(obj, "upstreamShare",
				sankey.getLinkShare(provider, node));
		edges.add(obj);
	}
}
