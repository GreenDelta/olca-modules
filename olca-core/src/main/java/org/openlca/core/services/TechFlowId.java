package org.openlca.core.services;

import java.util.Objects;
import java.util.Optional;

import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.results.LcaResult;
import org.openlca.util.Strings;

public record TechFlowId(String providerId, String flowId) {

	public static TechFlowId of(String providerId, String flowId) {
		return new TechFlowId(providerId, flowId);
	}

	public static TechFlowId fromString(String s) {
		if (Strings.nullOrEmpty(s))
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
