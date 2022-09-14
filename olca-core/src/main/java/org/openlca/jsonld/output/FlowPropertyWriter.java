package org.openlca.jsonld.output;

import org.openlca.core.model.FlowProperty;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

public record FlowPropertyWriter(JsonExport exp)
		implements JsonWriter<FlowProperty> {

	@Override
	public JsonObject write(FlowProperty prop) {
		var obj = Util.init(prop);
		Json.put(obj, "flowPropertyType", prop.flowPropertyType);
		Json.put(obj, "unitGroup", exp.handleRef(prop.unitGroup));
		return obj;
	}
}
