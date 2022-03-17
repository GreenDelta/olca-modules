package org.openlca.ipc.handlers;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.Contribution;
import org.openlca.core.results.FlowValue;
import org.openlca.core.results.LocationResult;
import org.openlca.core.results.UpstreamNode;
import org.openlca.core.results.UpstreamTree;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.ipc.handlers.Upstream.StringPair;

import com.google.gson.JsonArray;

public class InventoryHandler {

	private final Utils utils;

	public InventoryHandler(HandlerContext context) {
		this.utils = new Utils(context);
	}

	@Rpc("get/inventory/inputs")
	public RpcResponse getInputs(RpcRequest req) {
		return getInventory(req, true);
	}

	@Rpc("get/inventory/outputs")
	public RpcResponse getOutputs(RpcRequest req) {
		return getInventory(req, false);
	}

	private RpcResponse getInventory(RpcRequest req, boolean input) {
		return utils.simple(req, (result, cache) -> {
			List<FlowValue> data = new ArrayList<>();
			result.getTotalFlowResults().forEach(r -> {
				if (r.isInput() == input && r.value() != 0) {
					data.add(r);
				}
			});
			return JsonRpc.encode(data, r -> JsonRpc.encode(r, cache));
		});
	}

	@Rpc("get/inventory/contributions/processes")
	public RpcResponse getProcessContributions(RpcRequest req) {
		return utils.contributionFlow(req, (result, flow, cache) -> {
			List<Contribution<RootDescriptor>> contributions = result
					.getProcessContributions(flow);
			contributions = utils.filter(contributions, contribution -> contribution.amount != 0);
			String unit = utils.getUnit(flow, cache);
			return JsonRpc.encode(contributions, cache, json -> json.addProperty("unit", unit));
		});
	}

	@Rpc("get/inventory/contributions/locations")
	public RpcResponse getLocationContributions(RpcRequest req) {
		return utils.contributionFlow(req, (result, flow, cache) -> {
			LocationResult r = new LocationResult(result, cache.db);
			List<Contribution<LocationDescriptor>> cons = utils
					.toDescriptors(r.getContributions(flow.flow()));
			cons = utils.filter(cons, c -> c.amount != 0);
			String unit = utils.getUnit(flow, cache);
			return JsonRpc.encode(cons, cache,
					json -> json.addProperty("unit", unit));
		});
	}

	@Rpc("get/inventory/contributions/location/processes")
	public RpcResponse getProcessContributionsForLocation(RpcRequest req) {
		return utils.contributionFlowLocation(req, (result, flow, location, cache) -> {
			List<Contribution<RootDescriptor>> contributions = result
					.getProcessContributions(flow);
			contributions = utils.filter(contributions, contribution -> {
				if (contribution.item instanceof ProcessDescriptor) {
					if (((ProcessDescriptor) contribution.item).location != location.id) {
						return false;
					}
				}
				return contribution.amount != 0;
			});
			String unit = utils.getUnit(flow, cache);
			return JsonRpc.encode(contributions, cache, json -> json.addProperty("unit", unit));
		});
	}

	@Rpc("get/inventory/total_requirements")
	public RpcResponse getTotalRequirements(RpcRequest req) {
		return utils.contribution(req,
			(result, cache) -> JsonRpc.encode(
				result.totalRequirements(), null, result.techIndex(), cache));
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
		return utils.fullProcess(req, (result, process, cache) -> {
			JsonArray contributions = new JsonArray();
			result.enviIndex().each((i, f) -> {
				if (f.isInput() != input)
					return;
				double total = result.getTotalFlowResult(f);
				if (total == 0)
					return;
				Contribution<FlowDescriptor> c = new Contribution<>();
				c.item = f.flow();
				c.amount = result.getDirectFlowResult(process, f);
				c.share = c.amount / total;
				if (c.amount == 0)
					return;
				String unit = utils.getUnit(f, cache);
				contributions.add(JsonRpc.encode(c, cache, json -> {
					json.addProperty("unit", unit);
					json.addProperty("upstream",
							result.getUpstreamFlowResult(process, f));
				}));
			});

			return contributions;
		});
	}

	@Rpc("get/inventory/upstream")
	public RpcResponse getUpstream(RpcRequest req) {
		return utils.fullFlow(req, (result, flow, cache) -> {
			List<StringPair> products = utils.parseProducts(req);
			UpstreamTree tree = result.getTree(flow);
			List<UpstreamNode> results = Upstream.calculate(tree, products);
			String unit = utils.getUnit(flow, cache);
			return JsonRpc.encode(results, tree, cache, json -> {
				json.addProperty("unit", unit);
				json.add("upstream", json.remove("amount"));
			});
		});
	}

}
