package org.openlca.jsonld.output;

import java.util.List;

import org.openlca.core.model.ParameterRedef;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

class ParameterRedefs {

	/**
	 * Maps the given parameter redefinitions to a JSON array. If necessary, it
	 * exports the respective parameter context (i.e. global parameters or LCIA
	 * category parameters).
	 */
	static JsonArray map(List<ParameterRedef> redefs, JsonExport exp) {
		var array = new JsonArray();
		for (var p : redefs) {
			JsonObject obj = new JsonObject();
			array.add(obj);
			Json.put(obj, "name", p.name);
			Json.put(obj, "value", p.value);
			Json.put(obj, "uncertainty", Uncertainties.map(p.uncertainty));
			Json.put(obj, "isProtected", p.isProtected);
			if (p.contextId != null && p.contextType != null) {
				Json.put(obj, "context", exp.handleRef(p.contextType, p.contextId));
			}
		}
		return array;
	}
}
