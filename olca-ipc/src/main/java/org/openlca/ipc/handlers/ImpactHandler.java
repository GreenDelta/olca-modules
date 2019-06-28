package org.openlca.ipc.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.ContributionItem;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.ImpactResult;
import org.openlca.core.results.LocationContribution;
import org.openlca.core.results.UpstreamNode;
import org.openlca.core.results.UpstreamTree;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.ipc.handlers.Upstream.StringPair;

import com.google.gson.JsonArray;

public class ImpactHandler {

	private final Utils utils;

	public ImpactHandler(HandlerContext context) {
		this.utils = new Utils(context);
	}

	@Rpc("get/impacts")
	public RpcResponse getImpacts(RpcRequest req) {
		return utils.simple(req, (result, cache) -> {
			List<ImpactResult> impacts = result.getTotalImpactResults();
			impacts = utils.filter(impacts, impact -> impact.value != 0);
			return JsonRpc.encode(impacts, r -> JsonRpc.encode(r, cache));
		});
	}

	@Rpc("get/impacts/contributions/flows")
	public RpcResponse getFlowContributions(RpcRequest req) {
		return utils.contributionImpact(req, (result, impact, cache) -> {
			double total = result.getTotalImpactResult(impact);
			List<ContributionItem<FlowDescriptor>> contributions = new ArrayList<>();
			result.getFlows().forEach(flow -> {
				ContributionItem<FlowDescriptor> c = new ContributionItem<>();
				c.item = flow;
				c.amount = result.getDirectFlowImpact(flow, impact);
				c.share = c.amount / total;
				if (c.amount == 0)
					return;
				contributions.add(c);
			});
			return JsonRpc.encode(contributions, cache, json -> json.addProperty("unit", impact.referenceUnit));
		});
	}

	@Rpc("get/impacts/contributions/process/flows")
	public RpcResponse getFlowContributionsForProcess(RpcRequest req) {
		return utils.contributionImpactProcess(req, (result, impact, process, cache) -> {
			double total = result.getDirectImpactResult(process, impact);
			List<ContributionItem<FlowDescriptor>> contributions = new ArrayList<>();
			result.getFlows().forEach(flow -> {
				ContributionItem<FlowDescriptor> c = new ContributionItem<>();
				c.item = flow;
				c.amount = result.getDirectFlowResult(process, flow) * getImpactFactor(result, impact, flow);
				c.share = c.amount / total;
				if (c.amount == 0)
					return;
				contributions.add(c);
			});
			return JsonRpc.encode(contributions, cache, json -> json.addProperty("unit", impact.referenceUnit));
		});
	}

	@Rpc("get/impacts/contributions/location/flows")
	public RpcResponse getFlowContributionsForLocation(RpcRequest req) {
		return utils.contributionImpactLocation(req, (result, impact, location, cache) -> {
			List<ContributionItem<ProcessDescriptor>> contributions = new ArrayList<>();
			// TODO
			contributions = utils.filter(contributions, contribution -> contribution.amount != 0);
			return JsonRpc.encode(contributions, cache, json -> json.addProperty("unit", impact.referenceUnit));
		});
	}

	@Rpc("get/impacts/contributions/location/process/flows")
	public RpcResponse getFlowContributionsForLocationAndProcess(RpcRequest req) {
		return utils.contributionImpactLocationProcess(req, (result, impact, location, process, cache) -> {
			List<ContributionItem<ProcessDescriptor>> contributions = new ArrayList<>();
			// TODO
			contributions = utils.filter(contributions, contribution -> contribution.amount != 0);
			return JsonRpc.encode(contributions, cache, json -> json.addProperty("unit", impact.referenceUnit));
		});
	}

	@Rpc("get/impacts/contributions/processes")
	public RpcResponse getProcessContributions(RpcRequest req) {
		return utils.contributionImpact(req, (result, impact, cache) -> {
			double total = result.getTotalImpactResult(impact);
			Map<String, ContributionItem<CategorizedDescriptor>> contributions = new HashMap<>();
			result.getProcesses().forEach(process -> {
				ContributionItem<CategorizedDescriptor> c = new ContributionItem<>();
				c.item = process;
				c.amount = result.getDirectImpactResult(process, impact);
				c.share = c.amount / total;
				if (c.amount == 0)
					return;
				contributions.put(process.refId, c);
			});
			return JsonRpc.encode(contributions.values(), cache,
					json -> json.addProperty("unit", impact.referenceUnit));
		});
	}

	@Rpc("get/impacts/contributions/location/processes")
	public RpcResponse getProcessContributionsForLocation(RpcRequest req) {
		return utils.contributionImpactLocation(req, (result, impact, location, cache) -> {
			List<ContributionItem<ProcessDescriptor>> contributions = new ArrayList<>();
			// TODO
			contributions = utils.filter(contributions, contribution -> contribution.amount != 0);
			return JsonRpc.encode(contributions, cache, json -> json.addProperty("unit", impact.referenceUnit));
		});
	}

	@Rpc("get/impacts/contributions/locations")
	public RpcResponse getLocationContributions(RpcRequest req) {
		return utils.contributionImpact(req, (result, impact, cache) -> {
			LocationContribution calculator = new LocationContribution(result, cache);
			List<ContributionItem<LocationDescriptor>> contributions = utils
					.toDescriptors(calculator.calculate(impact).contributions);
			contributions = utils.filter(contributions, contribution -> contribution.amount != 0);
			return JsonRpc.encode(contributions, cache, json -> json.addProperty("unit", impact.referenceUnit));
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
				ContributionItem<ImpactCategoryDescriptor> c = new ContributionItem<>();
				c.item = impact;
				c.amount = result.getDirectImpactResult(process, impact);
				c.share = c.amount / total;
				if (c.amount == 0)
					return;
				contributions.add(JsonRpc.encode(c, cache, json -> {
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

	private double getImpactFactor(ContributionResult result, ImpactCategoryDescriptor impact, FlowDescriptor flow) {
		int row = result.impactIndex.of(impact);
		int col = result.flowIndex.of(flow);
		double value = result.impactFactors.get(row, col);
		if (result.isInput(flow)) {
			// characterization factors for input flows are negative in the
			// matrix. A simple abs() is not correct because the original
			// characterization factor maybe was already negative (-(-(f))).
			value = -value;
		}
		return value;
	}

}
