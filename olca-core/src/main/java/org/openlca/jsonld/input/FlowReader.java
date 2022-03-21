package org.openlca.jsonld.input;

import java.util.HashMap;
import java.util.Objects;

import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.jsonld.Json;

public record FlowReader(EntityResolver resolver)
	implements EntityReader<Flow> {

	public FlowReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public Flow read(JsonObject json) {
		var flow = new Flow();
		update(flow, json);
		return flow;
	}

	@Override
	public void update(Flow flow, JsonObject json) {
		Util.mapBase(flow, json, resolver);
		flow.flowType = Json.getEnum(json, "flowType", FlowType.class);
		flow.casNumber = Json.getString(json, "cas");
		flow.synonyms = Json.getString(json, "synonyms");
		flow.formula = Json.getString(json, "formula");
		flow.infrastructureFlow = Json.getBool(json, "isInfrastructureFlow", false);
		var locId = Json.getRefId(json, "location");
		if (locId != null) {
			flow.location = resolver.get(Location.class, locId);
		}
		mapPropertyFactors(flow, json);
	}

	private void mapPropertyFactors(Flow flow, JsonObject json) {

		// sync with existing flow property factors. we identify
		// them by their flow property ID.
		var oldFactors = new HashMap<String, FlowPropertyFactor>(
			flow.flowPropertyFactors.size());
		for (var factor : flow.flowPropertyFactors) {
			if (factor.flowProperty == null)
				continue;
			oldFactors.put(factor.flowProperty.refId, factor);
		}
		flow.flowPropertyFactors.clear();

		var array = Json.getArray(json, "flowProperties");
		for (var e : array) {

			// get the flow property
			if (!e.isJsonObject())
				continue;
			var factorJson = e.getAsJsonObject();
			var propId = Json.getRefId(factorJson, "flowProperty");
			var property = resolver.get(FlowProperty.class, propId);
			if (property == null)
				continue;

			// create or update the factor
			var factor = oldFactors.getOrDefault(
				propId, new FlowPropertyFactor());
			factor.flowProperty = property;
			factor.conversionFactor = Json.getDouble(
				factorJson, "conversionFactor", 1.0);
			flow.flowPropertyFactors.add(factor);

			// check if it is the reference flow property
			boolean isRef = Json.getBool(factorJson, "isRefFlowProperty", false);
			if (isRef) {
				flow.referenceFlowProperty = property;
			}
		}
	}
}
