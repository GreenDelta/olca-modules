package org.openlca.ipc.handlers;

import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public class CostHandler {

	private final HandlerContext context;

	public CostHandler(HandlerContext context) {
		this.context = context;
	}

	@Rpc("get/costs/direct")
	public RpcResponse getTotalRequirements(RpcRequest req) {
		return ResultRequest.of(req, context, rr -> {
			var result = rr.result();
			if (!result.hasCosts())
				return rr.noCostResults();
			var array = JsonRpc.arrayOf(result.techIndex(), techFlow -> {
				var obj = new JsonObject();
				Json.put(obj, "provider", JsonRpc.encodeTechFlow(techFlow, rr.refs()));
				Json.put(obj, "amount", result.totalRequirementsOf(techFlow));
				Json.put(obj, "costs", result.directCostsOf(techFlow));
				return obj;
			});
			return Responses.ok(array, req);
		});
	}

	@Rpc("get/costs/upstream/added_value")
	public RpcResponse getUpstreamAddedValue(RpcRequest req) {
		return ResultRequest.of(req, context, rr -> {
			var result = rr.result();
			if (!result.hasCosts())
				return rr.noCostResults();
			var tree = result.getAddedValueTree();
			return Upstream.getNodesForPath(rr, tree);
		});
	}

	@Rpc("get/costs/upstream/net_costs")
	public RpcResponse getUpstreamNetCosts(RpcRequest req) {
		return ResultRequest.of(req, context, rr -> {
			var result = rr.result();
			if (!result.hasCosts())
				return rr.noCostResults();
			var tree = result.getCostTree();
			return Upstream.getNodesForPath(rr, tree);
		});
	}
}
