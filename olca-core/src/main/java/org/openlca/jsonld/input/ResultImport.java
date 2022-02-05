package org.openlca.jsonld.input;

import com.google.gson.JsonObject;
import org.openlca.core.model.FlowResult;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Result;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

class ResultImport extends BaseImport<Result> {

	private ResultImport(String refId, JsonImport conf) {
		super(ModelType.RESULT, refId, conf);
	}

	static Result run(String refId, JsonImport conf) {
		return new ResultImport(refId, conf).run();
	}

	@Override
	Result map(JsonObject json, long id) {
		if (json == null)
			return null;
		var result = new Result();
		In.mapAtts(json, result, id, conf);
		result.urn = Json.getString(json, "urn");
		var methodId = Json.getRefId(json, "impactMethod");
		if (Strings.notEmpty(methodId)) {
			result.impactMethod = ImpactMethodImport.run(refId, conf);
		}
		readImpactResults(json, result);

		return result;

	}

	private void readImpactResults(JsonObject json, Result result) {
		var array = Json.getArray(json, "impactResults");
		if (array == null)
			return;
		for (var e : array) {
			if (!e.isJsonObject())
				continue;
			var obj = e.getAsJsonObject();
			var r = new ImpactResult();
			var impactId = Json.getRefId(obj, "indicator");
			if (Strings.notEmpty(impactId)) {
				r.indicator = ImpactCategoryImport.run(impactId, conf);
			}
			r.amount = Json.getDouble(obj, "amount", 0);
			r.description = Json.getString(obj, "description");
			result.impactResults.add(r);
		}
	}

	private void readFlowResults(JsonObject json, Result result) {
		var array = Json.getArray(json, "flowResults");
		if (array == null)
			return;
		for (var e : array) {
			if (!e.isJsonObject())
				continue;
			var obj = e.getAsJsonObject();
			var r = new FlowResult();
			result.flowResults.add(r);


		}
	}
}
