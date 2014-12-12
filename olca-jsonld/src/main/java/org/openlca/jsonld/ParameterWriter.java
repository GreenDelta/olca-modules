package org.openlca.jsonld;

import java.lang.reflect.Type;

import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Uncertainty;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class ParameterWriter implements JsonSerializer<Parameter> {

	@Override
	public JsonElement serialize(Parameter parameter, Type type,
			JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		JsonWriter.addContext(obj);
		map(parameter, obj);
		return obj;
	}

	static void map(Parameter parameter, JsonObject obj) {
		if (parameter == null || obj == null)
			return;
		obj.addProperty("@type", "Parameter");
		if (parameter.getId() != 0)
			obj.addProperty("@id", parameter.getId());
		obj.addProperty("name", parameter.getName());
		obj.addProperty("description", parameter.getDescription());
		obj.addProperty("parameterScope", getScope(parameter));
		obj.addProperty("inputParameter", parameter.isInputParameter());
		obj.addProperty("value", parameter.getValue());
		obj.addProperty("formula", parameter.getFormula());
		obj.addProperty("externalSource", parameter.getExternalSource());
		obj.addProperty("sourceType", parameter.getSourceType());
		mapUncertainty(parameter, obj);
	}

	private static void mapUncertainty(Parameter parameter, JsonObject obj) {
		Uncertainty uncertainty = parameter.getUncertainty();
		if (uncertainty == null)
			return;
		JsonObject uncertaintyObj = new JsonObject();
		UncertaintyWriter.map(uncertainty, uncertaintyObj);
		obj.add("uncertainty", uncertaintyObj);
	}

	private static String getScope(Parameter parameter) {
		ParameterScope scope = parameter.getScope();
		if (scope == null)
			return "GLOBAL_SCOPE";
		switch (scope) {
		case GLOBAL:
			return "GLOBAL_SCOPE";
		case IMPACT_METHOD:
			return "LCIA_METHOD_SCOPE";
		case PROCESS:
			return "PROCESS_SCOPE";
		default:
			return "GLOBAL_SCOPE";
		}
	}
}
