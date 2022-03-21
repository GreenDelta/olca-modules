package org.openlca.jsonld.output;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowResult;
import org.openlca.core.model.Result;
import org.openlca.core.model.Unit;
import org.openlca.jsonld.Json;

import java.util.Objects;

record ResultWriter(JsonExport exp) implements Writer<Result> {

	@Override
	public JsonObject write(Result result) {
		var json = Writer.init(result);
		Json.put(json, "productSystem", exp.handleRef(result.productSystem));
		Json.put(json, "impactMethod", exp.handleRef(result.impactMethod));
		writeImpactResults(result, json);
		writeFlowResults(result, json);
		return json;
	}

	private void writeImpactResults(Result result, JsonObject json) {
		if (result.impactResults.isEmpty())
			return;
		var array = new JsonArray();
		for (var r : result.impactResults) {
			var obj = new JsonObject();
			Json.put(obj, "indicator", exp.handleRef(r.indicator));
			Json.put(obj, "amount", r.amount);
			Json.put(obj, "description", r.description);
			array.add(obj);
		}
		json.add("impactResults", array);
	}

	private void writeFlowResults(Result result, JsonObject json) {
		if (result.flowResults.isEmpty())
			return;
		var array = new JsonArray();
		for (var r : result.flowResults) {
			var obj = new JsonObject();

			// object references
			Json.put(obj, "flow", exp.handleRef(r.flow));
			Json.put(obj, "flowProperty", exp.handleRef(propertyOf(r)));
			Json.put(obj, "unit",Json.asRef(unitOf(r)));
			Json.put(obj, "location", exp.handleRef(r.location));

			// other attributes
			Json.put(obj, "isInput", r.isInput);
			if (Objects.equals(r, result.referenceFlow)) {
				Json.put(obj, "isRefFlow", true);
			}
			Json.put(obj, "amount", r.amount);
			Json.put(obj, "description", r.description);

			array.add(obj);
		}
		json.add("flowResults", array);
	}

	private FlowProperty propertyOf(FlowResult r) {
		var factor = r.flowPropertyFactor;
		if (factor != null && factor.flowProperty != null)
			return factor.flowProperty;
		return r.flow != null
			? r.flow.referenceFlowProperty
			: null;
	}

	private Unit unitOf(FlowResult r) {
		if (r.unit != null)
			return r.unit;
		return r.flow != null
			? r.flow.getReferenceUnit()
			: null;
	}
}
