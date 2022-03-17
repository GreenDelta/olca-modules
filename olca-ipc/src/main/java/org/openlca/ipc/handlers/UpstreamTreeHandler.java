package org.openlca.ipc.handlers;

import java.util.Objects;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.UpstreamNode;
import org.openlca.core.results.UpstreamTree;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class UpstreamTreeHandler {

	private final HandlerContext context;

	public UpstreamTreeHandler(HandlerContext context) {
		this.context = context;
	}

	@Rpc("get/upstream/tree")
	public RpcResponse getUpstreamTree(RpcRequest req) {
		var params = req.params;
		if (params == null || !params.isJsonObject())
			return Responses.badRequest("invalid parameters", req);

		// get the result
		var obj = params.getAsJsonObject();
		var resultId = Json.getString(obj, "resultId");
		if (resultId == null)
			return Responses.badRequest("no result ID", req);
		var result = resultOf(resultId);
		if (result == null)
			return Responses.badRequest(
				"no full result cached for ID=" + resultId, req);

		// get the result ref
		var refObj = Json.getObject(obj, "ref");
		if (refObj == null)
			return Responses.badRequest(
				"no result reference given", req);
		var refId = Json.getString(refObj, "@id");
		if (refId == null)
			return Responses.badRequest(
				"invalid result reference: no ID", req);
		var refType = refType(refObj);
		if (refType == null)
			return Responses.badRequest(
				"invalid result reference type", req);
		var tree = refType == ModelType.IMPACT_CATEGORY
			? impactTree(result, refId)
			: flowTree(result, refId);
		if (tree == null)
			return Responses.badRequest(
				"invalid result reference: does not exist", req);

		// expand the tree and return it
		var jsonTree = Expansion.of(context.db, obj).expand(tree);
		return Responses.ok(jsonTree, req);
	}

	private FullResult resultOf(String resultId) {
		var obj = context.cache.get(resultId);
		if (obj instanceof FullResult)
			return (FullResult) obj;
		if (obj instanceof CachedResult<?> cachedResult) {
			if (cachedResult.result instanceof FullResult r)
				return r;
		}
		return null;
	}

	private ModelType refType(JsonObject refObj) {
		if (refObj == null)
			return null;
		var type = Json.getString(refObj, "@type");
		if (type == null)
			return null;
		if (type.equalsIgnoreCase("ImpactCategory"))
			return ModelType.IMPACT_CATEGORY;
		if (type.equalsIgnoreCase("Flow"))
			return ModelType.FLOW;
		return null;
	}

	private UpstreamTree impactTree(FullResult result, String impactID) {
		var impactIdx = result.impactIndex();
		if (impactIdx == null)
			return null;
		for (int i = 0; i < impactIdx.size(); i++) {
			var impact = impactIdx.at(i);
			if (Strings.nullOrEqual(impact.refId, impactID))
				return result.getTree(impact);
		}
		return null;
	}

	private UpstreamTree flowTree(FullResult result, String flowID) {
		var flowIdx = result.enviIndex();
		if (flowIdx == null)
			return null;
		for (int i = 0; i < flowIdx.size(); i++) {
			var flow = flowIdx.at(i);
			if (flow.location() != null)  // regionalization not supported here
				continue;
			if (Strings.nullOrEqual(flow.flow().refId, flowID))
				return result.getTree(flow);
		}
		return null;
	}

	private static class Expansion {

		final EntityCache cache;
		final int maxDepth;
		final double minContribution;
		final int maxRecursionDepth;

		Expansion(IDatabase db,
							int maxDepth,
							double minContribution,
							int maxRecursionDepth) {
			this.cache = EntityCache.create(db);
			this.maxDepth = maxDepth;
			this.minContribution = minContribution;
			this.maxRecursionDepth = maxRecursionDepth;
		}

		static Expansion of(IDatabase db, JsonObject obj) {
			return new Expansion(db,
				Json.getInt(obj, "maxDepth", 5),
				Json.getDouble(obj, "minContribution", 0.1),
				Json.getInt(obj, "maxRecursionDepth", 3));
		}

		JsonObject expand(UpstreamTree tree) {
			var treeObj = new JsonObject();
			if (tree.ref instanceof EnviFlow ef) {
				treeObj.add("ref", JsonRef.of(ef.flow(), cache));
			} else if (tree.ref instanceof Descriptor d) {
				treeObj.add("ref", JsonRef.of(d, cache));
			}
			var root = initJsonOf(tree.root);
			expand(root, tree, new Path(tree.root));
			treeObj.add("root", root);
			return treeObj;
		}

		private void expand(JsonObject parent, UpstreamTree tree, Path path) {

			var node = path.node;
			double result = path.node.result();

			// first check if we need to cut the path here
			if (result == 0)
				return;
			if (maxDepth > 0 && path.length > maxDepth)
				return;
			var totalResult = tree.root.result();
			if (minContribution > 0 && totalResult != 0) {
				double c = Math.abs(result / totalResult);
				if (c < minContribution)
					return;
			}
			if (maxDepth < 0) {
				int count = path.count(node.provider());
				if (count > maxRecursionDepth) {
					return;
				}
			}

			// expand the child nodes
			var childArray = new JsonArray();
			for (var child : tree.childs(node)) {
				var childJson = initJsonOf(child);
				expand(childJson, tree, path.append(child));
				childArray.add(childJson);
			}
			if (childArray.size() > 0) {
				parent.add("childs", childArray);
			}
		}

		private JsonObject initJsonOf(UpstreamNode node) {
			if (node == null || node.provider() == null)
				return null;
			var json = new JsonObject();
			var process = JsonRef.of(node.provider().provider(), cache);
			var flow = JsonRef.of(node.provider().flow(), cache);
			var product = new JsonObject();
			product.add("process", process);
			product.add("flow", flow);
			json.add("product", product);
			json.addProperty("result", node.result());
			return json;
		}
	}

	private static class Path {
		final Path prefix;
		final UpstreamNode node;
		final int length;

		Path(UpstreamNode node) {
			this.prefix = null;
			this.node = node;
			this.length = 0;
		}

		Path(UpstreamNode node, Path prefix) {
			this.prefix = prefix;
			this.node = node;
			this.length = 1 + prefix.length;
		}

		Path append(UpstreamNode node) {
			return new Path(node, this);
		}

		int count(TechFlow product) {
			int c = Objects.equals(product, node.provider()) ? 1 : 0;
			return prefix != null ? c + prefix.count(product) : c;
		}
	}

}
