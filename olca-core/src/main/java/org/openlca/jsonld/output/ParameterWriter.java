package org.openlca.jsonld.output;

import org.openlca.core.model.Parameter;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

class ParameterWriter extends Writer<Parameter> {

	ParameterWriter(JsonExport exp) {
		super(exp);
	}

	@Override
	JsonObject write(Parameter param) {
		JsonObject obj = super.write(param);
		if (obj == null)
			return null;
		mapAttr(obj, param);
		GlobalParameters.sync(param, exp);
		return obj;
	}

	static void mapAttr(JsonObject json, Parameter param) {
		addBasicAttributes(param, json);
		Json.put(json, "parameterScope", param.scope);
		Json.put(json, "isInputParameter", param.isInputParameter);
		Json.put(json, "value", param.value);
		Json.put(json, "formula", param.formula);
		Json.put(json, "uncertainty", Uncertainties.map(param.uncertainty));
	}

}
