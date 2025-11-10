package org.openlca.core.services;

import com.google.gson.JsonObject;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.LcaResult;
import org.openlca.jsonld.Json;
import org.openlca.commons.Strings;

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
		if (Strings.isNotBlank(impactId)) {
			var r = Util.impactCategoryOf(result, impactId);
			if (r.isError())
				return Response.error(r.error());
			req.impact = r.value();
		}

		var flowRef = Json.getObject(obj, "enviFlow");
		if (flowRef != null) {
			var enviFlowId = EnviFlowId.of(flowRef).orElse(null);
			if (enviFlowId == null)
				return Response.error("invalid enviFlow object");
			req.flow = enviFlowId.findEnviFlowOf(result).orElse(null);
			if (req.flow == null)
				return Response.error("no envi-flow available for: " + enviFlowId);
		}

		// we set some defaults here
		req.minShare = Json.getDouble(obj, "minShare", 0.01);
		req.maxNodes = Json.getInt(obj, "maxNodes", 100);

		return Response.of(req);
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
