package org.openlca.jsonld.output;

import org.openlca.core.model.FlowProperty;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record FlowPropertyWriter(JsonExport exp)
		implements JsonWriter<FlowProperty> {

	@Override
	public JsonObject write(FlowProperty prop) {
		var obj = Util.init(exp, prop);
		Json.put(obj, "flowPropertyType", prop.flowPropertyType);
		Json.put(obj, "unitGroup", exp.handleRef(prop.unitGroup));
		return obj;
	}
}
