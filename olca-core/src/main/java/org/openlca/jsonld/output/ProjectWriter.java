package org.openlca.jsonld.output;

import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

record ProjectWriter(JsonExport exp) implements Writer<Project> {

	@Override
	public JsonObject write(Project p) {
		JsonObject obj = Writer.init(p);
		Json.put(obj, "isWithCosts", p.isWithCosts);
		Json.put(obj, "isWithRegionalization", p.isWithRegionalization);
		Json.put(obj, "impactMethod", exp.handleRef(p.impactMethod));
		Json.put(obj, "nwSet", Json.asRef(p.nwSet));
		mapVariants(obj, p);
		GlobalParameters.sync(p, exp);
		return obj;
	}

	private void mapVariants(JsonObject json, Project p) {
		var array = new JsonArray();
		for (ProjectVariant v : p.variants) {
			var obj = new JsonObject();
			array.add(obj);
			Json.put(obj, "name", v.name);
			Json.put(obj, "productSystem", exp.handleRef(v.productSystem));
			Json.put(obj, "amount", v.amount);
			Json.put(obj, "unit", Json.asRef(v.unit));
			Json.put(obj, "allocationMethod", v.allocationMethod);
			Json.put(obj, "description", v.description);
			Json.put(obj, "isDisabled", v.isDisabled);
			var prop = v.flowPropertyFactor != null
				? v.flowPropertyFactor.flowProperty
				: null;
			Json.put(obj, "flowProperty", exp.handleRef(prop));
			if (!v.parameterRedefs.isEmpty()) {
				var redefs = Util.mapRedefs(v.parameterRedefs, exp);
				Json.put(obj, "parameterRedefs", redefs);
			}
		}
		Json.put(json, "variants", array);
	}

}
