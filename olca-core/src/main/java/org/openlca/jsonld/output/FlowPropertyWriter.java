package org.openlca.jsonld.output;

import org.openlca.core.model.FlowProperty;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

record FlowPropertyWriter(JsonExport exp) implements Writer<FlowProperty> {

	@Override
	public JsonObject write(FlowProperty prop) {
		var obj = Writer.init(prop);
		Json.put(obj, "flowPropertyType", prop.flowPropertyType);
		Json.put(obj, "unitGroup", exp.handleRef(prop.unitGroup));
		return obj;
	}
}
