package org.openlca.ipc.handlers;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.Contribution;
import org.openlca.core.results.LocationResult;
import org.openlca.core.services.JsonResultService;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

	@Rpc("get/inventory/total_requirements")
	public RpcResponse getTotalRequirements(RpcRequest req) {
		return ResultRequest.of(req, context, rr -> {
			var array = rr.result().techIndex()
					.stream()
					.map(techFlow -> {
						var obj = new JsonObject();
						Json.put(obj, "provider", JsonRpc.encodeTechFlow(techFlow, rr.refs()));
						Json.put(obj, "amount", rr.result().totalRequirementsOf(techFlow));
						return obj;
					})
					.collect(JsonRpc.toArray());
			return Responses.ok(array, req);
		});
	}

	@Rpc("get/inventory/process_results/inputs")
	public RpcResponse getProcessResultsInputs(RpcRequest req) {
		return getProcessResults(req, true);
	}

	@Rpc("get/inventory/process_results/outputs")
	public RpcResponse getProcessResultsOutputs(RpcRequest req) {
		return getProcessResults(req, false);
	}

	private RpcResponse getProcessResults(RpcRequest req, boolean input) {
		return ResultRequest.of(req, context, rr -> {
			var result = rr.result();
			var techFlow = rr.techFlow();
			if (techFlow == null)
				return rr.providerMissing();

			var array = new JsonArray();
			if (!result.hasEnviFlows())
				return Responses.ok(array, req);

			for (var enviFlow : result.enviIndex()) {
				if (enviFlow.isInput() != input)
					continue;
				double total = result.totalFlowOf(enviFlow);
				var c = new Contribution<EnviFlow>();
				c.item = enviFlow;
				c.amount = result.getDirectFlowResult(techFlow, enviFlow);
				c.computeShare(total);
				var obj = JsonRpc.encodeContribution(
						c, ef -> JsonRpc.encodeEnviFlow(ef, rr.refs()));
				Json.put(obj, "upstream",
						result.totalFlowOf(techFlow, enviFlow));
				array.add(obj);
			}
			return Responses.ok(array, req);
		});
	}

	@Rpc("get/inventory/upstream")
	public RpcResponse getUpstream(RpcRequest req) {
		return ResultRequest.of(req, context, rr -> {
			if (rr.enviFlow() == null)
				return rr.enviFlowMissing();
			var tree = rr.result().getTree(rr.enviFlow());
			return Upstream.getNodesForPath(rr, tree);
		});
	}

}
