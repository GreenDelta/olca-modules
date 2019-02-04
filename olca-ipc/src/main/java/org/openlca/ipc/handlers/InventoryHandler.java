package org.openlca.ipc.handlers;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.ContributionItem;
import org.openlca.core.results.FlowResult;
import org.openlca.core.results.LocationContribution;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;

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
		return utils.handle1(req, (result, cache) -> {
			List<FlowResult> data = new ArrayList<>();
			result.getTotalFlowResults().forEach(r -> {
				if (r.input == input) {
					data.add(r);
				}
			});
			return JsonRpc.encode(data, r -> JsonRpc.encode(r, cache));
		});
	}

	@Rpc("get/inventory/contributions/processes")
	public RpcResponse getProcessContributions(RpcRequest req) {
		return utils.handle2(req, (result, flow, cache) -> {
			List<ContributionItem<CategorizedDescriptor>> contributions = result
					.getProcessContributions(flow).contributions;
			return JsonRpc.encode(contributions, cache, json -> json.addProperty("unit", utils.getUnit(flow, cache)));
		});
	}

	@Rpc("get/inventory/contributions/locations")
	public RpcResponse getLocationContributions(RpcRequest req) {
		return utils.handle2(req, (result, flow, cache) -> {
			LocationContribution calculator = new LocationContribution(result, cache);
			List<ContributionItem<LocationDescriptor>> contributions = utils
					.toDescriptorContributions(calculator.calculate(flow).contributions);
			String unit = utils.getUnit(flow, cache);
			return JsonRpc.encode(contributions, cache, json -> json.addProperty("unit", unit));
		});
	}

	@Rpc("get/inventory/contributions/location/processes")
	public RpcResponse getProcessContributionsForLocation(RpcRequest req) {
		return utils.handle3(req, (result, flow, location, cache) -> {
			List<ContributionItem<ProcessDescriptor>> contributions = new ArrayList<>();
			// TODO
			String unit = utils.getUnit(flow, cache);
			return JsonRpc.encode(contributions, cache, json -> json.addProperty("unit", unit));
		});
	}

	@Rpc("get/total_requirements")
	public RpcResponse getTotalRequirements(RpcRequest req) {
		return utils.handle1(req, (result, cache) -> {
			return JsonRpc.encode(result.totalRequirements, result.techIndex, cache);
		});
	}

	@Rpc("get/process_results/inputs")
	public RpcResponse getProcessResultsInputs(RpcRequest req) {
		return getProcessResults(req, true);
	}

	@Rpc("get/process_results/outputs")
	public RpcResponse getProcessResultsOutputs(RpcRequest req) {
		return getProcessResults(req, false);
	}

	private RpcResponse getProcessResults(RpcRequest req, boolean input) {
		return utils.handle8(req, (result, process, cache) -> {
			JsonArray contributions = new JsonArray();
			result.getFlows().forEach(flow -> {
				if (result.isInput(flow) != input)
					return;
				double total = result.getTotalFlowResult(flow);
				if (total == 0)
					return;
				ContributionItem<FlowDescriptor> c = new ContributionItem<>();
				c.item = flow;
				c.amount = result.getDirectFlowResult(process, flow);
				c.share = c.amount / total;
				if (c.amount == 0)
					return;
				contributions.add(JsonRpc.encode(c, cache, json -> {
					json.addProperty("unit", utils.getUnit(flow, cache));
					json.addProperty("upstream", result.getUpstreamFlowResult(process, flow));
				}));
			});
			return contributions;
		});
	}

}
