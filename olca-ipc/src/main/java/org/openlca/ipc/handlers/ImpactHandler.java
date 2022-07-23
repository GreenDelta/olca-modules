package org.openlca.ipc.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.Contribution;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.ImpactValue;
import org.openlca.core.results.LocationResult;
import org.openlca.core.results.UpstreamNode;
import org.openlca.core.results.UpstreamTree;
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
		return context.requireResult(req, cachedResult ->
			cachedResult.result()
			.getTotalImpactResults()
			.stream()
			.filter(r -> r.value() != 0)
			.map(r -> JsonRpc.encodeImpactValue(r, cachedResult.refs()))
			.collect(JsonRpc.toArray()));
	}

	@Rpc("get/impacts/contributions/flows")
	public RpcResponse getFlowContributions(RpcRequest req) {
		return utils.contributionImpact(req, (result, impact, cache) -> {
			double total = result.getTotalImpactResult(impact);
			List<Contribution<FlowDescriptor>> contributions = new ArrayList<>();
			// TODO: regionalization
			result.enviIndex().each((i, f) -> {
				var c = new Contribution<FlowDescriptor>();
				c.item = f.flow();
				c.amount = result.getDirectFlowImpact(f, impact);
				c.share = c.amount / total;
				if (c.amount == 0)
					return;
				contributions.add(c);
			});
			return JsonRpc.encodeResult(contributions, cache,
					json -> json.addProperty("unit", impact.referenceUnit));
		});
	}

	@Rpc("get/impacts/contributions/process/flows")
	public RpcResponse getFlowContributionsForProcess(RpcRequest req) {
		return utils.contributionImpactProcess(req, (result, impact, process, cache) -> {
			double total = result.getDirectImpactResult(process, impact);
			List<Contribution<FlowDescriptor>> contributions = new ArrayList<>();
			// TODO: regionalization
			result.enviIndex().each((i, f) -> {
				var c = new Contribution<FlowDescriptor>();
				c.item = f.flow();
				c.amount = result.getDirectFlowResult(process, f)
						* getImpactFactor(result, impact, f);
				c.share = c.amount / total;
				if (c.amount == 0)
					return;
				contributions.add(c);
			});
			return JsonRpc.encodeResult(contributions, cache,
					json -> json.addProperty("unit", impact.referenceUnit));
		});
	}

	@Rpc("get/impacts/contributions/location/flows")
	public RpcResponse getFlowContributionsForLocation(RpcRequest req) {
		return utils.contributionImpactLocation(req, (result, impact, location, cache) -> {
			List<Contribution<ProcessDescriptor>> contributions = new ArrayList<>();
			// TODO
			contributions = utils.filter(contributions, contribution -> contribution.amount != 0);
			return JsonRpc.encodeResult(contributions, cache, json -> json.addProperty("unit", impact.referenceUnit));
		});
	}

	@Rpc("get/impacts/contributions/location/process/flows")
	public RpcResponse getFlowContributionsForLocationAndProcess(RpcRequest req) {
		return utils.contributionImpactLocationProcess(req, (result, impact, location, process, cache) -> {
			List<Contribution<ProcessDescriptor>> contributions = new ArrayList<>();
			// TODO
			contributions = utils.filter(contributions, contribution -> contribution.amount != 0);
			return JsonRpc.encodeResult(contributions, cache, json -> json.addProperty("unit", impact.referenceUnit));
		});
	}

	@Rpc("get/impacts/contributions/processes")
	public RpcResponse getProcessContributions(RpcRequest req) {
		return utils.contributionImpact(req, (result, impact, cache) -> {
			double total = result.getTotalImpactResult(impact);
			Map<String, Contribution<RootDescriptor>> contributions = new HashMap<>();
			result.getProcesses().forEach(process -> {
				Contribution<RootDescriptor> c = new Contribution<>();
				c.item = process;
				c.amount = result.getDirectImpactResult(process, impact);
				c.share = c.amount / total;
				if (c.amount == 0)
					return;
				contributions.put(process.refId, c);
			});
			return JsonRpc.encodeResult(contributions.values(), cache,
					json -> json.addProperty("unit", impact.referenceUnit));
		});
	}

	@Rpc("get/impacts/contributions/location/processes")
	public RpcResponse getProcessContributionsForLocation(RpcRequest req) {
		return utils.contributionImpactLocation(req, (result, impact, location, cache) -> {
			List<Contribution<ProcessDescriptor>> contributions = new ArrayList<>();
			// TODO
			contributions = utils.filter(contributions, contribution -> contribution.amount != 0);
			return JsonRpc.encodeResult(contributions, cache, json -> json.addProperty("unit", impact.referenceUnit));
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
