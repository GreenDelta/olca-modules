package org.openlca.ipc.handlers;

import org.openlca.core.services.JsonResultService;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;

public class InventoryHandler {

	private final JsonResultService results;

	public InventoryHandler(HandlerContext context) {
		this.results = context.results();
	}

	@Rpc("result/total-flows")
	public RpcResponse getTotalFlows(RpcRequest req) {
		return ResultRequest.of(req, rr -> results.getTotalFlows(rr.id()));
	}

	@Rpc("result/total-flow-value-of")
	public RpcResponse getTotalFlowValueOf(RpcRequest req) {
		return ResultRequest.of(req,
				rr -> results.getTotalFlowValueOf(rr.id(), rr.enviFlow()));
	}

	@Rpc("result/total-flow-values-of")
	public RpcResponse getTotalFlowValuesOf(RpcRequest req) {
		return ResultRequest.of(req,
				rr -> results.getTotalFlowValuesOf(rr.id(), rr.enviFlow()));
	}

	@Rpc("results/direct-flow-values-of")
	public RpcResponse getDirectFlowValuesOf(RpcRequest req) {
		return ResultRequest.of(req,
				rr -> results.getDirectFlowValuesOf(rr.id(), rr.enviFlow()));
	}

	@Rpc("result/direct-flows-of")
	public RpcResponse getDirectFlowsOf(RpcRequest req) {
		return ResultRequest.of(req,
				rr -> results.getDirectFlowsOf(rr.id(), rr.techFlow()));
	}

	@Rpc("result/direct-flow-of")
	public RpcResponse getDirectFlowOf(RpcRequest req) {
		return ResultRequest.of(req,
				rr -> results.getDirectFlowOf(rr.id(), rr.enviFlow(), rr.techFlow()));
	}

	@Rpc("result/total-flows-of")
	public RpcResponse getTotalFlowsOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalFlowsOf(rr.id(), rr.techFlow()));
	}

	@Rpc("result/total-flow-of")
	public RpcResponse getTotalFlowOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalFlowOf(rr.id(), rr.enviFlow(), rr.techFlow()));
	}

	@Rpc("result/total-requirements")
	public RpcResponse getTotalRequirements(RpcRequest req) {
		return ResultRequest.of(req, rr -> results.getTotalRequirements(rr.id()));
	}

	@Rpc("get/inventory/upstream")
	public RpcResponse getUpstream(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getUpstreamOfEnviFlow(rr.id(), rr.path(), rr.enviFlow()));
	}

}
