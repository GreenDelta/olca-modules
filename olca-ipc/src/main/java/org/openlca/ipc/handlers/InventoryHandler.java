package org.openlca.ipc.handlers;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.Contribution;
import org.openlca.core.results.LocationResult;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class InventoryHandler {

	private final HandlerContext context;

	public InventoryHandler(HandlerContext context) {
		this.context = context;
	}

	@Rpc("get/inventory/inputs")
	public RpcResponse getInputs(RpcRequest req) {
		return ResultRequest.of(req, context,
				rr -> Responses.ok(inventoryOf(rr, true), req));
	}

	@Rpc("get/inventory/outputs")
	public RpcResponse getOutputs(RpcRequest req) {
		return ResultRequest.of(req, context,
				rr -> Responses.ok(inventoryOf(rr, false), req));
	}

	private JsonArray inventoryOf(ResultRequest rr, boolean input) {
		var result = rr.result();
		if (!result.hasEnviFlows())
			return new JsonArray();
		return result.totalFlows()
				.stream()
				.filter(r -> r.isInput() == input && r.value() != 0)
				.map(v -> JsonRpc.encodeFlowValue(v, rr.refs()))
				.collect(JsonRpc.toArray());
	}

	@Rpc("get/inventory/contributions/processes")
	public RpcResponse getProcessContributions(RpcRequest req) {
		return ResultRequest.of(req, context, rr -> {
			if (rr.enviFlow() == null)
				return rr.enviFlowMissing();
			var array = rr.result().getProcessContributions(rr.enviFlow())
					.stream()
					.filter(c -> c.amount != 0)
					.map(c -> JsonRpc.encodeContribution(c,
							t -> JsonRpc.encodeTechFlow(t, rr.refs())))
					.collect(JsonRpc.toArray());
			return Responses.ok(array, req);
		});
	}

	@Rpc("get/inventory/contributions/locations")
	public RpcResponse getLocationContributions(RpcRequest req) {
		return ResultRequest.of(req, context, rr -> {
			if (rr.enviFlow() == null)
				return rr.enviFlowMissing();
			var r = new LocationResult(rr.result(), context.db());
			var array = r.getContributions(rr.enviFlow().flow()).stream()
					.filter(c -> c.amount != 0)
					.map(c -> JsonRpc.encodeContribution(c, Json::asRef))
					.collect(JsonRpc.toArray());
			return Responses.ok(array, req);
		});
	}

	@Rpc("get/inventory/contributions/location/processes")
	public RpcResponse getProcessContributionsForLocation(RpcRequest req) {
		return ResultRequest.of(req, context, rr -> {
			if (rr.enviFlow() == null)
				return rr.enviFlowMissing();
			var locRefId = Json.getRefId(rr.requestParameter(), "location");
			var loc = locRefId != null
					? context.db().getDescriptor(Location.class, locRefId)
					: null;
			var array = rr.result().getProcessContributions(rr.enviFlow())
					.stream()
					.filter(c -> {
						if (!(c.item.provider() instanceof ProcessDescriptor p))
							return false;
						return (loc == null && p.location == null)
								|| (loc != null && p.location != null && loc.id == p.location);
					})
					.map(c -> JsonRpc.encodeContribution(
							c, t -> JsonRpc.encodeTechFlow(t, rr.refs())))
					.collect(JsonRpc.toArray());
			return Responses.ok(array, req);
		});
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
						result.getUpstreamFlowResult(techFlow, enviFlow));
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
