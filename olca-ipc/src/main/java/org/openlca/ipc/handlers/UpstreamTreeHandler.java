package org.openlca.ipc.handlers;

import java.util.Objects;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.results.LcaResult;
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
		return ResultRequest.of(req, context, rr -> {
			var result = rr.result();

			// get the result ref
			// TODO: to also support regionalized flows the result reference in case
			// of environmental flows should be an EnviFlow reference object
			var refObj = Json.getObject(rr.requestParameter(), "ref");
			if (refObj == null)
				return Responses.badRequest("no result reference given", req);
			var refId = Json.getRefId(refObj, "@id");
			if (refId == null)
				return Responses.badRequest("invalid result reference: no ID", req);
			var refType = refType(refObj);
			if (refType == null)
				return Responses.badRequest("invalid result reference type", req);
			var tree = refType == ModelType.IMPACT_CATEGORY
					? impactTree(result, refId)
					: flowTree(result, refId);
			if (tree == null)
				return Responses.badRequest(
						"invalid result reference: does not exist", req);

			// expand the tree and return it
			var jsonTree = Expansion.of(rr).expand(tree);
			return Responses.ok(jsonTree, req);
		});
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

	private UpstreamTree impactTree(LcaResult result, String impactID) {
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

	private UpstreamTree flowTree(LcaResult result, String flowID) {
		var flowIdx = result.enviIndex();
		if (flowIdx == null)
			return null;
		for (int i = 0; i < flowIdx.size(); i++) {
			var flow = flowIdx.at(i);
			if (flow.location() != null)  // TODO: regionalization not supported here
				continue;
			if (Strings.nullOrEqual(flow.flow().refId, flowID))
				return result.getTree(flow);
		}
		return null;
	}

	private static class Expansion {

		final ResultRequest rr;
		final int maxDepth;
		final double minContribution;
		final int maxRecursionDepth;

		Expansion(ResultRequest rr,
				int maxDepth,
				double minContribution,
				int maxRecursionDepth) {
			this.rr = rr;
			this.maxDepth = maxDepth;
			this.minContribution = minContribution;
			this.maxRecursionDepth = maxRecursionDepth;
		}

		static Expansion of(ResultRequest rr) {
			var param = rr.requestParameter();
			return new Expansion(rr,
					Json.getInt(param, "maxDepth", 5),
					Json.getDouble(param, "minContribution", 0.1),
					Json.getInt(param, "maxRecursionDepth", 3));
		}

		JsonObject expand(UpstreamTree tree) {
			var treeObj = new JsonObject();
			if (tree.ref instanceof EnviFlow ef) {
				treeObj.add("ref", JsonRpc.encodeEnviFlow(ef, rr.refs()));
			} else if (tree.ref instanceof RootDescriptor d) {
				treeObj.add("ref", rr.refs().asRef(d));
			}
			var root = Upstream.encodeNode(rr, tree.root);
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
				var childJson = Upstream.encodeNode(rr, child);
				expand(childJson, tree, path.append(child));
				childArray.add(childJson);
			}
			if (childArray.size() > 0) {
				parent.add("childs", childArray);
			}
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
