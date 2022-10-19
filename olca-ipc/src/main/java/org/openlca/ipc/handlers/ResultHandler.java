package org.openlca.ipc.handlers;

import org.openlca.core.services.JsonResultService;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;

public class ResultHandler {

	private final JsonResultService results;

	public ResultHandler(HandlerContext context) {
		this.results = context.results();
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

	@Rpc("result/total-requirements")
	public RpcResponse getTotalRequirements(RpcRequest req) {
		return ResultRequest.of(req, rr -> results.getTotalRequirements(rr.id()));
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

	@Rpc("result/direct-flow-values-of")
	public RpcResponse getDirectFlowValuesOf(RpcRequest req) {
		return ResultRequest.of(req,
				rr -> results.getDirectFlowValuesOf(rr.id(), rr.enviFlow()));
	}

	@Rpc("result/total-flow-values-of")
	public RpcResponse getTotalFlowValuesOf(RpcRequest req) {
		return ResultRequest.of(req,
				rr -> results.getTotalFlowValuesOf(rr.id(), rr.enviFlow()));
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

	@Rpc("result/total-flows-of-one")
	public RpcResponse getTotalFlowsOfOne(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalFlowsOfOne(rr.id(), rr.techFlow()));
	}

	@Rpc("result/total-flow-of-one")
	public RpcResponse getTotalFlowOfOne(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalFlowOfOne(rr.id(), rr.enviFlow(), rr.techFlow()));
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

	@Rpc("result/direct-impact-values-of")
	public RpcResponse getDirectImpactValuesOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getDirectImpactValuesOf(rr.id(), rr.impact()));
	}

	@Rpc("result/total-impact-values-of")
	public RpcResponse getTotalImpactValuesOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalImpactValuesOf(rr.id(), rr.impact()));
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

	@Rpc("result/total-impacts-of-one")
	public RpcResponse getTotalImpactsOfOne(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalImpactsOfOne(rr.id(), rr.techFlow()));
	}

	@Rpc("result/total-impact-of-one")
	public RpcResponse getTotalImpactOfOne(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalImpactOfOne(rr.id(), rr.impact(), rr.techFlow()));
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

	@Rpc("result/flow-impacts-of-one")
	public RpcResponse getFlowImpactsOfOne(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getFlowImpactsOfOne(rr.id(), rr.enviFlow()));
	}

	@Rpc("result/flow-impacts-of")
	public RpcResponse getFlowImpactsOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getFlowImpactsOf(rr.id(), rr.enviFlow()));
	}

	@Rpc("result/flow-impact-of")
	public RpcResponse getFlowImpactOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getFlowImpactOf(rr.id(), rr.impact(), rr.enviFlow()));
	}

	@Rpc("result/flow-impact-values-of")
	public RpcResponse getFlowImpactValuesOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getFlowImpactValuesOf(rr.id(), rr.impact()));
	}

	// endregion

	// region: cost results

	@Rpc("result/total-costs")
	public RpcResponse getTotalCosts(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalCosts(rr.id()));
	}

	@Rpc("result/direct-cost-values")
	public RpcResponse getDirectCostValues(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getDirectCostValues(rr.id()));
	}

	@Rpc("result/total-cost-values")
	public RpcResponse getTotalCostValues(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalCostValues(rr.id()));
	}

	@Rpc("result/direct-costs-of")
	public RpcResponse getDirectCostsOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getDirectCostsOf(rr.id(), rr.techFlow()));
	}

	@Rpc("result/total-costs-of-one")
	public RpcResponse getTotalCostsOfOne(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalCostsOfOne(rr.id(), rr.techFlow()));
	}

	@Rpc("result/total-costs-of")
	public RpcResponse getTotalCostsOf(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getTotalCostsOf(rr.id(), rr.techFlow()));
	}

	// endregion

	// region: upstream trees

	@Rpc("result/upstream-of-flow")
	public RpcResponse getUpstream(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getUpstreamOfEnviFlow(rr.id(), rr.path(), rr.enviFlow()));
	}

	@Rpc("result/upstream-of-impact-category")
	public RpcResponse getUpstreamOfImpactCategory(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getUpstreamOfImpactCategory(rr.id(), rr.path(), rr.impact()));
	}

	@Rpc("result/upstream-of-costs")
	public RpcResponse getUpstreamOfCosts(RpcRequest req) {
		return ResultRequest.of(req, rr ->
				results.getUpstreamOfCosts(rr.id(), rr.path()));
	}

	// endregion

}
