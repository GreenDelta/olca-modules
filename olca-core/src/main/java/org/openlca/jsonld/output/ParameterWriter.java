package org.openlca.jsonld.output;

import java.util.List;

import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public record ParameterWriter(JsonExport exp) implements JsonWriter<Parameter> {

	@Override
	public JsonObject write(Parameter param) {
		var obj = new JsonObject();
		mapAttr(obj, param);
		Util.mapOtherProperties(param, obj);
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

	/**
	 * Maps the given parameter redefinitions to a JSON array. If necessary, it
	 * exports the respective parameter context (i.e. global parameters or LCIA
	 * category parameters).
	 */
	public static JsonArray mapRedefs(JsonExport exp, List<ParameterRedef> redefs) {
		var array = new JsonArray();
		if (exp == null || redefs == null)
			return array;
		for (var p : redefs) {
			array.add(mapRedef(exp, p));
		}
		return array;
	}

	/**
	 * Maps the given parameter redefinition to a JSON object. If necessary, it
	 * exports the respective parameter context (i.e. global parameters or LCIA
	 * category parameters).
	 * 
	 * Also used in onlinelca
	 */
	public static JsonObject mapRedef(JsonExport exp, ParameterRedef p) {
		var obj = new JsonObject();
		Json.put(obj, "name", p.name);
		Json.put(obj, "value", p.value);
		Json.put(obj, "uncertainty", Uncertainties.map(p.uncertainty));
		Json.put(obj, "isProtected", p.isProtected);
		if (p.contextId != null && p.contextType != null) {
			Json.put(obj, "context", exp.handleRef(p.contextType, p.contextId));
		}
		return obj;
	}

}
