package org.openlca.ipc.handlers;

import org.openlca.core.services.JsonResultService;
import org.openlca.core.services.Response;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;

public class ResultHandler {

	private final JsonResultService results;

	public ResultHandler(HandlerContext context) {
		this.results = context.results();
	}

	@Rpc("result/calculate")
	public RpcResponse calculate(RpcRequest req) {
		var obj = req.requireJsonObject();
		if (!obj.isValue())
			return Responses.of(obj, req);
		var state = results.calculate(obj.value());
		return Responses.of(state, req);
	}

	@Rpc("result/simulate")
	public RpcResponse simulate(RpcRequest req) {
		var obj = req.requireJsonObject();
		if (!obj.isValue())
			return Responses.of(obj, req);
		var state = results.simulate(obj.value());
		return Responses.of(state, req);
	}

	@Rpc("result/simulate/next")
	public RpcResponse simulateNext(RpcRequest req) {
		return ResultRequest.of(req, rr -> results.nextSimulationOf(rr.id()));
	}

	@Rpc("result/state")
	public RpcResponse getState(RpcRequest req) {
		return ResultRequest.of(req, rr -> results.getState(rr.id()));
	}

	@Rpc("result/dispose")
	public RpcResponse dispose(RpcRequest req) {
		return ResultRequest.of(req, rr -> results.dispose(rr.id()));
	}

	// region: index elements

	@Rpc("result/tech-flows")
	public RpcResponse getTechFlows(RpcRequest req) {
		return ResultRequest.of(req, rr -> results.getTechFlows(rr.id()));
	}

	@Rpc("result/envi-flows")
	public RpcResponse getEnviFlows(RpcRequest req) {
		return ResultRequest.of(req, rr -> results.getEnviFlows(rr.id()));
	}

	@Rpc("result/impact-categories")
	public RpcResponse getImpactCategories(RpcRequest req) {
		return ResultRequest.of(req, rr -> results.getImpactCategories(rr.id()));
	}

	// endregion

	// region: tech flows

	@Rpc("result/demand")
	public RpcResponse getDemand(RpcRequest req) {
		return ResultRequest.of(req, rr -> results.getDemand(rr.id()));
	}

	@Rpc("result/scaling-factors")
	public RpcResponse getScalingFactors(RpcRequest req) {
		return ResultRequest.of(req, rr -> results.getScalingFactors(rr.id()));
	}

	@Rpc("result/totality-factors")
	public RpcResponse getTotalityFactors(RpcRequest req) {
		return ResultRequest.of(req,
				rr -> results.getTotalityFactors(rr.id()));
	}

	@Rpc("result/totality-factor-of")
	public RpcResponse getTotalityFactorOf(RpcRequest req) {
		return ResultRequest.of(req,
				rr -> results.getTotalityFactorOf(rr.id(), rr.techFlow()));
	}

	@Rpc("result/total-requirements")
	public RpcResponse getTotalRequirements(RpcRequest req) {
		return ResultRequest.of(req, rr -> results.getTotalRequirements(rr.id()));
	}

	@Rpc("result/total-requirements-of")
	public RpcResponse getTotalRequirementsOf(RpcRequest req) {
		return ResultRequest.of(req,
				rr -> results.getTotalRequirementsOf(rr.id(), rr.techFlow()));
	}

	@Rpc("result/scaled-tech-flows-of")
	public RpcResponse getScaledTechFlowsOf(RpcRequest req) {
		return ResultRequest.of(req,
				rr -> results.getScaledTechFlowsOf(rr.id(), rr.techFlow()));
	}

	@Rpc("result/unscaled-tech-flows-of")
	public RpcResponse getUnscaledTechFlowsOf(RpcRequest req) {
		return ResultRequest.of(req,
				rr -> results.getUnscaledTechFlowsOf(rr.id(), rr.techFlow()));
	}

	// endregion

	// region: flow results

	@Rpc("result/total-flows")
	public RpcResponse getTotalFlows(RpcRequest req) {
		return ResultRequest.of(req, rr -> results.getTotalFlows(rr.id()));
	}

	@Rpc("result/total-flow-value-of")
	public RpcResponse getTotalFlowValueOf(RpcRequest req) {
		return ResultRequest.of(req,
				rr -> results.getTotalFlowValueOf(rr.id(), rr.enviFlow()));
	}

	@Rpc("result/flow-contributions-of")
	public RpcResponse getFlowContributionsOf(RpcRequest req) {
		return ResultRequest.of(req,
				rr -> results.getFlowContributionsOf(rr.id(), rr.enviFlow()));
	}

	@Rpc("result/direct-interventions-of")
	public RpcResponse getDirectInterventionsOf(RpcRequest req) {
		return ResultRequest.of(req,
				rr -> results.getDirectInterventionsOf(rr.id(), rr.techFlow()));
	}

	@Rpc("result/direct-intervention-of")
	public RpcResponse getDirectInterventionOf(RpcRequest req) {
		return ResultRequest.of(req,
				rr -> results.getDirectInterventionOf(
						rr.id(), rr.enviFlow(), rr.techFlow()));
	}

	@Rpc("result/flow-intensities-of")
	public RpcResponse getFlowIntensitiesOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getFlowIntensitiesOf(rr.id(), rr.techFlow()));
	}

	@Rpc("result/flow-intensity-of")
	public RpcResponse getFlowIntensityOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getFlowIntensityOf(rr.id(), rr.enviFlow(), rr.techFlow()));
	}

	@Rpc("result/total-interventions-of")
	public RpcResponse getTotalInterventionsOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalInterventionsOf(rr.id(), rr.techFlow()));
	}

	@Rpc("result/total-intervention-of")
	public RpcResponse getTotalInterventionOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalInterventionOf(rr.id(), rr.enviFlow(), rr.techFlow()));
	}

	@Rpc("result/upstream-interventions-of")
	public RpcResponse getUpstreamInterventionsOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getUpstreamInterventionsOf(rr.id(), rr.path(), rr.enviFlow()));
	}

	@Rpc("result/grouped-flow-results-of")
	public RpcResponse getGroupedFlowResultsOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getGroupedFlowResultsOf(rr.id(), rr.enviFlow()));
	}

	// endregion

	// region: impact results

	@Rpc("result/total-impacts")
	public RpcResponse getTotalImpacts(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalImpacts(rr.id()));
	}

	@Rpc("result/total-impact-value-of")
	public RpcResponse getTotalImpactValueOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalImpactValueOf(rr.id(), rr.impact()));
	}

	@Rpc("result/total-impacts/normalized")
	public RpcResponse getNormalizedImpacts(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getNormalizedImpacts(rr.id()));
	}

	@Rpc("result/total-impacts/weighted")
	public RpcResponse getWeightedImpacts(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getWeightedImpacts(rr.id()));
	}

	@Rpc("result/impact-contributions-of")
	public RpcResponse getImpactContributionsOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getImpactContributionsOf(rr.id(), rr.impact()));
	}

	@Rpc("result/direct-impacts-of")
	public RpcResponse getDirectImpactsOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getDirectImpactsOf(rr.id(), rr.techFlow()));
	}

	@Rpc("result/direct-impact-of")
	public RpcResponse getDirectImpactOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getDirectImpactOf(rr.id(), rr.impact(), rr.techFlow()));
	}

	@Rpc("result/impact-intensities-of")
	public RpcResponse getImpactIntensitiesOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getImpactIntensitiesOf(rr.id(), rr.techFlow()));
	}

	@Rpc("result/impact-intensity-of")
	public RpcResponse getImpactIntensityOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getImpactIntensityOf(rr.id(), rr.impact(), rr.techFlow()));
	}

	@Rpc("result/total-impacts-of")
	public RpcResponse getTotalImpactsOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalImpactsOf(rr.id(), rr.techFlow()));
	}

	@Rpc("result/total-impact-of")
	public RpcResponse getTotalImpactOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalImpactOf(rr.id(), rr.impact(), rr.techFlow()));
	}

	@Rpc("result/impact-factors-of")
	public RpcResponse getImpactFactorsOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getImpactFactorsOf(rr.id(), rr.impact()));
	}

	@Rpc("result/impact-factor-of")
	public RpcResponse getImpactFactorOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getImpactFactorOf(rr.id(), rr.impact(), rr.enviFlow()));
	}

	@Rpc("result/flow-impacts-of")
	public RpcResponse getFlowImpactsOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getFlowImpactsOf(rr.id(), rr.impact()));
	}

	@Rpc("result/flow-impact-of")
	public RpcResponse getFlowImpactOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getFlowImpactOf(rr.id(), rr.impact(), rr.enviFlow()));
	}

	@Rpc("result/upstream-impacts-of")
	public RpcResponse getUpstreamImpactsOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getUpstreamImpactsOf(rr.id(), rr.path(), rr.impact()));
	}

	@Rpc("result/grouped-impact-results-of")
	public RpcResponse getGroupedImpactResultsOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getGroupedImpactResultsOf(rr.id(), rr.impact()));
	}

	// endregion

	// region: cost results

	@Rpc("result/total-costs")
	public RpcResponse getTotalCosts(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalCosts(rr.id()));
	}

	@Rpc("result/cost-contributions")
	public RpcResponse getCostContributions(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getCostContributions(rr.id()));
	}

	@Rpc("result/direct-costs-of")
	public RpcResponse getDirectCostsOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getDirectCostsOf(rr.id(), rr.techFlow()));
	}

	@Rpc("result/cost-intensities-of")
	public RpcResponse getCostIntensitiesOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getCostIntensitiesOf(rr.id(), rr.techFlow()));
	}

	@Rpc("result/total-costs-of")
	public RpcResponse getTotalCostsOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalCostsOf(rr.id(), rr.techFlow()));
	}

	@Rpc("result/upstream-costs-of")
	public RpcResponse getUpstreamCostsOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getUpstreamCostsOf(rr.id(), rr.path()));
	}

	@Rpc("result/grouped-cost-results")
	public RpcResponse getGroupedCostResults(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getGroupedCostResults(rr.id()));
	}

	// endregion

	@Rpc("result/sankey")
	public RpcResponse getSankeyGraph(RpcRequest req) {
		return ResultRequest.of(req, rr -> {
			var config = rr.param("config");
			return config != null && config.isJsonObject()
					? results.getSankeyGraph(rr.id(), config.getAsJsonObject())
					: Response.error("invalid request; 'config: SankeyRequest' missing");
		});
	}
}
