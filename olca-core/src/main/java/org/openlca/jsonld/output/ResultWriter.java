package org.openlca.jsonld.output;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowResult;
import org.openlca.core.model.Result;
import org.openlca.core.model.Unit;

import java.util.Objects;

class ResultWriter extends Writer<Result> {

	ResultWriter(ExportConfig config) {
		super(config);
	}

	@Override
	JsonObject write(Result result) {
		var json = super.write(result);
		if (json == null)
			return null;
		Out.put(json, "urn", result.urn);
		Out.put(json, "impactMethod", result.impactMethod, conf);
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
			Out.put(obj, "indicator", r.indicator, conf);
			Out.put(obj, "amount", r.amount);
			Out.put(obj, "description", r.description);
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
			Out.put(obj, "flow", r.flow, conf);
			Out.put(obj, "flowProperty", propertyOf(r), conf);
			Out.put(obj, "unit", unitOf(r), conf);
			Out.put(obj, "location", r.location, conf);

			// other attributes
			Out.put(obj, "isInput", r.isInput);
			if (Objects.equals(r, result.referenceFlow)) {
				Out.put(obj, "isReferenceFlow", true);
			}
			Out.put(obj, "amount", r.amount);
			Out.put(obj, "description", r.description);

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
