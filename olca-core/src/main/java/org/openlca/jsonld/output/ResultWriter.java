package org.openlca.jsonld.output;

import java.util.Objects;

import org.openlca.core.model.Result;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public record ResultWriter(JsonExport exp) implements JsonWriter<Result> {

	@Override
	public JsonObject write(Result result) {
		var json = Util.init(exp, result);
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
			if (r.flowPropertyFactor != null) {
				Json.put(obj, "flowProperty",
						exp.handleRef(r.flowPropertyFactor.flowProperty));
			}
			Json.put(obj, "unit", Json.asRef(r.unit));
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

}
