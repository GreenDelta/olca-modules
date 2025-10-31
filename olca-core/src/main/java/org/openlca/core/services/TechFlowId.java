package org.openlca.core.services;

import java.util.Objects;
import java.util.Optional;

import com.google.gson.JsonObject;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.results.LcaResult;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

public record TechFlowId(String providerId, String flowId) {

	public static TechFlowId of(String providerId, String flowId) {
		return new TechFlowId(providerId, flowId);
	}

	/**
	 * Extracts the identifiers of a technosphere flow from the given
	 * Json object. The given Json must follow the specification of
	 * a serialized technosphere flow, otherwise an empty option is
	 * returned.
	 */
	public static Optional<TechFlowId> of(JsonObject json) {
		if (json == null)
			return Optional.empty();
		var providerId = Json.getRefId(json, "provider");
		var flowId = Json.getRefId(json, "flow");
		return Strings.isBlank(providerId) || Strings.isBlank(flowId)
				? Optional.empty()
				: Optional.of(TechFlowId.of(providerId, flowId));
	}

	/**
	 * Parses the identifier of a technosphere flow from a string. In
	 * such a string, two colons are used to separate the provider ID
	 * from the flow ID: {@code <provider-id>::<flow-id>}.
	 */
	public static TechFlowId parse(String s) {
		if (Strings.isBlank(s))
			return new TechFlowId("", "");
		var parts = s.split("::");
		return parts.length < 2
				? TechFlowId.of(parts[0], "")
				: TechFlowId.of(parts[0], parts[1]);
	}

	public boolean matches(TechFlow techFlow) {
		if (techFlow == null)
			return false;
		if (techFlow.provider() == null || techFlow.flow() == null)
			return false;
		return Objects.equals(providerId, techFlow.provider().refId)
				&& Objects.equals(flowId, techFlow.flow().refId);
	}

	public Optional<TechFlow> findTechFlowOf(LcaResult result) {
		if (result == null)
			return Optional.empty();
		for (var techFlow : result.techIndex()) {
			if (matches(techFlow))
				return Optional.of(techFlow);
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		return providerId + "::" + flowId;
	}
}
