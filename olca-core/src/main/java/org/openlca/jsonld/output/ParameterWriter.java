package org.openlca.jsonld.output;

import org.openlca.core.model.Parameter;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

public record ParameterWriter(JsonExport exp) implements JsonWriter<Parameter> {

	@Override
	public JsonObject write(Parameter param) {
		var obj = new JsonObject();
		mapAttr(obj, param);
		GlobalParameters.sync(param, exp);
		return obj;
	}

	static void mapAttr(JsonObject json, Parameter param) {
		Util.mapBasicAttributes(param, json);
		Json.put(json, "parameterScope", param.scope);
		Json.put(json, "isInputParameter", param.isInputParameter);
		Json.put(json, "value", param.value);
		Json.put(json, "formula", param.formula);
		Json.put(json, "uncertainty", Uncertainties.map(param.uncertainty));
	}

}
