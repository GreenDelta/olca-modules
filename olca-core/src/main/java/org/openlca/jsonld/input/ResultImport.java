package org.openlca.jsonld.input;

import com.google.gson.JsonObject;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowResult;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Result;
import org.openlca.core.model.Unit;
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
		readFlowResults(json, result);
		return conf.db.put(result);

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
			r.amount = Json.getDouble(obj, "amount", 0);
			r.isInput = Json.getBool(obj, "isInput", false);
			boolean isRef = Json.getBool(obj, "isReferenceFlow", false);
			if (isRef) {
				result.referenceFlow = r;
			}

			// location
			var locationId = Json.getRefId(obj, "location");
			if (Strings.notEmpty(locationId)) {
				r.location = LocationImport.run(refId, conf);
			}

			// flow and unit
			var flowId = Json.getRefId(obj, "flow");
			if (Strings.nullOrEmpty(flowId))
				continue;
			r.flow = FlowImport.run(flowId, conf);
			r.flowPropertyFactor = propertyOf(r, obj);
			r.unit = unitOf(r, obj);
		}
	}

	private FlowPropertyFactor propertyOf(FlowResult r, JsonObject obj) {
		if (r.flow == null)
			return null;
		var propId = Json.getRefId(obj, "flowProperty");
		if (Strings.nullOrEmpty(propId))
			return r.flow.getReferenceFactor();
		for (var factor : r.flow.flowPropertyFactors) {
			var prop = factor.flowProperty;
			if (prop != null && propId.equals(prop.refId))
				return factor;
		}
		return null;
	}

	private Unit unitOf(FlowResult r, JsonObject obj) {
		if (r.flow == null
			|| r.flowPropertyFactor == null
			|| r.flowPropertyFactor.flowProperty == null)
			return null;
		var prop = r.flowPropertyFactor.flowProperty;
		var unitId = Json.getRefId(obj, "unit");
		if (Strings.nullOrEmpty(unitId))
			return prop.getReferenceUnit();
		if (prop.unitGroup == null)
			return null;
		for (var unit : prop.unitGroup.units) {
			if (unitId.equals(unit.refId))
				return unit;
		}
		return null;
	}

}