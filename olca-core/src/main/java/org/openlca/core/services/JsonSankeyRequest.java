package org.openlca.core.services;

import com.google.gson.JsonObject;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.LcaResult;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

import java.util.Objects;

class JsonSankeyRequest {

	private ImpactDescriptor impact;
	private EnviFlow flow;
	private boolean forCosts;
	private double minShare;
	private int maxNodes;

	private JsonSankeyRequest() {
	}

	static Response<JsonSankeyRequest> resolve(LcaResult result, JsonObject obj) {
		if (result == null)
			return Response.error("no result available");
		if (obj == null)
			return Response.error("invalid request");

		var req = new JsonSankeyRequest();
		req.forCosts = Json.getBool(obj, "forCosts", false);

		var impactId = Json.getRefId(obj, "impactCategory");
		if (Strings.notEmpty(impactId)) {
			req.impact = findImpact(result, impactId);
			if (req.impact == null)
				return Response.error("no impact category with @id="
						+ impactId + " available in result");
		}

		var flowRef = Json.getObject(obj, "enviFlow");
		if (flowRef != null) {
			req.flow = findFlow(result, flowRef);
			if (req.flow == null)
				return Response.error("invalid enviFlow object");
		}

		// we set some defaults here
		req.minShare = Json.getDouble(obj, "minShare", 0.01);
		req.maxNodes = Json.getInt(obj, "maxNodes", 100);

		return Response.of(req);
	}

	private static ImpactDescriptor findImpact(LcaResult result, String impactId) {
		if (!result.hasImpacts())
			return null;
		var impactIdx = result.impactIndex();
		if (impactIdx == null)
			return null;
		for (var impact : impactIdx) {
			if (Objects.equals(impactId, impact.refId))
				return impact;
		}
		return null;
	}

	private static EnviFlow findFlow(LcaResult result, JsonObject flowRef) {
		if (!result.hasEnviFlows())
			return null;
		var flowIdx = result.enviIndex();
		if (flowIdx == null)
			return null;
		var flowId = Json.getRefId(flowRef, "flow");
		var locationId = Json.getRefId(flowRef, "location");
		return new EnviFlowId(flowId, locationId)
				.findEnviFlowOf(result)
				.orElse(null);
	}

	boolean hasImpact() {
		return impact != null;
	}

	boolean hasFlow() {
		return flow != null;
	}

	boolean isForCosts() {
		return forCosts;
	}

	ImpactDescriptor impact() {
		return impact;
	}

	EnviFlow flow() {
		return flow;
	}

	double minShare() {
		return minShare;
	}

	int maxNodes() {
		return maxNodes;
	}
}
