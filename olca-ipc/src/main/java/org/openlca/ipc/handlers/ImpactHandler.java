package org.openlca.ipc.handlers;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.results.Contribution;
import org.openlca.core.results.LocationResult;
import org.openlca.core.results.UpstreamNode;
import org.openlca.core.results.UpstreamTree;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.ipc.handlers.Upstream.StringPair;

import com.google.gson.JsonArray;

public class ImpactHandler {

	private final HandlerContext context;
	private final Utils utils;

	public ImpactHandler(HandlerContext context) {
		this.context = context;
		this.utils = new Utils(context);
	}

	@Rpc("get/impacts")
	public RpcResponse getImpacts(RpcRequest req) {
		return ResultRequest.of(req, context, rr -> {
			var result = rr.result();
			if (!result.hasImpacts())
				return Responses.ok(new JsonArray(), req);
			var array = result.getTotalImpactResults().stream()
					.filter(r -> r.value() != 0)
					.map(r -> JsonRpc.encodeImpactValue(r, rr.refs()))
					.collect(JsonRpc.toArray());
			return Responses.ok(array, req);
		});
	}

	@Rpc("get/impacts/contributions/flows")
	public RpcResponse getFlowContributions(RpcRequest req) {
		return ResultRequest.of(req, context, rr -> {
			if (rr.impact() == null)
				return rr.impactMissing();
			var contributions = new ArrayList<Contribution<EnviFlow>>();
			double total = rr.result().getTotalImpactResult(rr.impact());
			for (var enviFlow : rr.result().enviIndex()) {
				var amount = rr.result().getDirectFlowImpact(enviFlow, rr.impact());
				if (amount == 0)
					continue;
				var c = new Contribution<EnviFlow>();
				c.item = enviFlow;
				c.amount = amount;
				c.share = total != 0 ? c.amount / total : 0;
				c.unit = rr.impact().referenceUnit;
				contributions.add(c);
			}
			var array = JsonRpc.encodeContributions(
					contributions,
					f -> JsonRpc.encodeEnviFlow(f, rr.refs()));
			return Responses.ok(array, req);
		});
	}

	@Rpc("get/impacts/contributions/process/flows")
	public RpcResponse getFlowContributionsForProcess(RpcRequest req) {
		return ResultRequest.of(req, context, rr -> {
			var techFlow = rr.techFlow();
			if (techFlow == null)
				return rr.providerMissing();
			var impact = rr.impact();
			if (impact == null)
				return rr.impactMissing();

			var result = rr.result();
			double total = result.getDirectImpactResult(techFlow, impact);
			var contributions = new ArrayList<Contribution<EnviFlow>>();
			for (var enviFlow : result.enviIndex()) {
				var factor = result.getImpactFactor(impact, enviFlow);
				var flowAmount = result.getDirectFlowResult(techFlow, enviFlow);
				var amount = factor * flowAmount;
				if (amount == 0)
					continue;
				var c = new Contribution<EnviFlow>();
				c.item = enviFlow;
				c.amount = amount;
				c.share = total != 0 ? c.amount / total : 0;
				c.unit = impact.referenceUnit;
				contributions.add(c);
			}
			var array = JsonRpc.encodeContributions(
					contributions,
					enviFlow -> JsonRpc.encodeEnviFlow(enviFlow, rr.refs()));
			return Responses.ok(array, req);
		});
	}

	@Rpc("get/impacts/contributions/processes")
	public RpcResponse getProcessContributions(RpcRequest req) {
		return ResultRequest.of(req, context, rr -> {
			if (rr.impact() == null)
				return rr.impactMissing();
			var result = rr.result();
			double total = result.getTotalImpactResult(rr.impact());
			var contributions = new ArrayList<Contribution<TechFlow>>();
			for (var techFlow : result.techIndex()) {
				var amount = result.getDirectImpactResult(techFlow, rr.impact());
				if (amount == 0)
					continue;
				var c = new Contribution<TechFlow>();
				c.item = techFlow;
				c.amount = amount;
				c.share = total != 0 ? amount / total : 0;
				c.unit = rr.impact().referenceUnit;
				contributions.add(c);
			}
			var array = JsonRpc.encodeContributions(
					contributions,
					techFlow -> JsonRpc.encodeTechFlow(techFlow, rr.refs()));
			return Responses.ok(array, req);
		});
	}

	@Rpc("get/impacts/contributions/locations")
	public RpcResponse getLocationContributions(RpcRequest req) {

		return utils.contributionImpact(req, (result, impact, cache) -> {
			LocationResult r = new LocationResult(result, cache.db);
			List<Contribution<LocationDescriptor>> contributions = utils
					.toDescriptors(r.getContributions(impact));
			contributions = utils.filter(contributions, contribution -> contribution.amount != 0);
			return JsonRpc.encodeResult(contributions, cache, json -> json.addProperty("unit", impact.referenceUnit));
		});
	}

	@Rpc("get/impacts/process_results")
	public RpcResponse getProcessResultsImpacts(RpcRequest req) {
		return utils.fullProcess(req, (result, process, cache) -> {
			JsonArray contributions = new JsonArray();
			result.getImpacts().forEach(impact -> {
				double total = result.getTotalImpactResult(impact);
				if (total == 0)
					return;
				Contribution<ImpactDescriptor> c = new Contribution<>();
				c.item = impact;
				c.amount = result.getDirectImpactResult(process, impact);
				c.share = c.amount / total;
				if (c.amount == 0)
					return;
				contributions.add(JsonRpc.encodeResult(c, cache, json -> {
					json.addProperty("unit", impact.referenceUnit);
					json.addProperty("upstream", result.getUpstreamImpactResult(process, impact));
				}));
			});
			return contributions;
		});
	}

	@Rpc("get/impacts/upstream")
	public RpcResponse getUpstream(RpcRequest req) {
		return utils.fullImpact(req, (result, impact, cache) -> {
			List<StringPair> products = utils.parseProducts(req);
			UpstreamTree tree = result.getTree(impact);
			List<UpstreamNode> results = Upstream.calculate(tree, products);
			return JsonRpc.encode(results, tree, cache, json -> {
				json.addProperty("unit", impact.referenceUnit);
				json.add("upstream", json.remove("amount"));
			});
		});
	}


}
