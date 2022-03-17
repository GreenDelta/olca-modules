package org.openlca.jsonld.output;

import org.openlca.core.model.FlowProperty;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

class FlowPropertyWriter extends Writer<FlowProperty> {

	FlowPropertyWriter(JsonExport exp) {
		super(exp);
	}

	@Override
	public JsonObject write(FlowProperty prop) {
		var obj = super.write(prop);
		if (obj == null)
			return null;
		Json.put(obj, "flowPropertyType", prop.flowPropertyType);
		Json.put(obj, "unitGroup", exp.handleRef(prop.unitGroup));
		return obj;
	}

}
