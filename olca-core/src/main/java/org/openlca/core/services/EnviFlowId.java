package org.openlca.core.services;

import java.util.Objects;
import java.util.Optional;

import com.google.gson.JsonObject;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.results.LcaResult;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

public record EnviFlowId(String flowId, String locationId) {

	public static EnviFlowId of(String flowId, String locationId) {
		return new EnviFlowId(flowId, locationId);
	}

	public static EnviFlowId of(String flowId) {
		return new EnviFlowId(flowId, null);
	}

	/**
	 * Extracts the identifiers of an elementary flow from the given
	 * Json object. The given Json must follow the specification of
	 * a serialized elementary flow, otherwise an empty option is
	 * returned.
	 */
	public static Optional<EnviFlowId> of(JsonObject json) {
		if (json == null)
			return Optional.empty();
		var flowId = Json.getRefId(json, "flow");
		if (Strings.isBlank(flowId))
			return Optional.empty();
		var locationId = Json.getRefId(json, "location");
		return Strings.isBlank(locationId)
				? Optional.of(EnviFlowId.of(flowId))
				: Optional.of(EnviFlowId.of(flowId, locationId));
	}

	/**
	 * Parses the identifier of an elementary flow from a string. In
	 * such a string, two colons are used to separate the flow ID
	 * from an optional location ID: {@code <provider-id>(::<flow-id>)?}.
	 */
	public static EnviFlowId parse(String s) {
		if (Strings.isBlank(s))
			return new EnviFlowId("", "");
		var parts = s.split("::");
		return parts.length < 2
				? EnviFlowId.of(parts[0])
				: EnviFlowId.of(parts[0], parts[1]);
	}

	public boolean matches(EnviFlow enviFlow) {
		if (enviFlow == null)
			return false;
		if (enviFlow.flow() == null
				|| !Objects.equals(enviFlow.flow().refId, flowId))
			return false;
		return enviFlow.location() != null
				? Objects.equals(enviFlow.location().refId, locationId)
				: Strings.isBlank(locationId);
	}

	public Optional<EnviFlow> findEnviFlowOf(LcaResult result) {
		if (result == null || !result.hasEnviFlows())
			return Optional.empty();
		for (var enviFlow : result.enviIndex()) {
			if (matches(enviFlow))
				return Optional.of(enviFlow);
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		return Strings.isBlank(locationId)
				? flowId
				: flowId + "::" + locationId;
	}
}
