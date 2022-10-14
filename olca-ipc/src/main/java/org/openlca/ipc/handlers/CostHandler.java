package org.openlca.ipc.handlers;

import org.openlca.core.services.JsonResultService;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;

public class CostHandler {

	private final HandlerContext context;
	private final JsonResultService results;

	public CostHandler(HandlerContext context) {
		this.context = context;
		this.results = context.results();
	}

	@Rpc("result/costs/direct")
	public RpcResponse getDirectCosts(RpcRequest req) {
		return ResultRequest.of(req, rr -> results.getDirectCosts(rr.id()));
	}

	@Rpc("result/costs/total")
	public RpcResponse getTotalCosts(RpcRequest req) {
		return ResultRequest.of(req, rr -> results.getTotalCosts(rr.id()));
	}

	@Rpc("result/costs/totals-by-tech-flows")
	public RpcResponse getTotalCostsByTechFlow(RpcRequest req) {
		return ResultRequest.of(req, rr -> results.getTotalCostsByTechFlow(rr.id()));
	}

	@Rpc("get/costs/upstream-tree-nodes")
	public RpcResponse getUpstreamAddedValue(RpcRequest req) {
		return ResultRequest.of(req, rr -> {
			var path = Json.getString(rr.requestParameter(), "path");
			return results.getUpstreamNodesForCosts(rr.id(), path);
		});
	}
}
