package org.openlca.jsonld.output;

import org.openlca.core.model.FlowProperty;

import com.google.gson.JsonObject;

class FlowPropertyWriter extends Writer<FlowProperty> {

	FlowPropertyWriter(ExportConfig conf) {
		super(conf);
	}
	
	@Override
	public JsonObject write(FlowProperty prop) {
		JsonObject obj = super.write(prop);
		if (obj == null)
			return null;
		Out.put(obj, "flowPropertyType", prop.getFlowPropertyType(), Out.REQUIRED_FIELD);
		Out.put(obj, "unitGroup", prop.getUnitGroup(), conf, Out.REQUIRED_FIELD);
		return obj;
	}

}
