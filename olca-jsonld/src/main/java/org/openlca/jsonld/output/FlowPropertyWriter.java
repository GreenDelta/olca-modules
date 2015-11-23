package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.RootEntity;

import com.google.gson.JsonObject;

class FlowPropertyWriter extends Writer<FlowProperty> {

	@Override
	public JsonObject write(FlowProperty prop, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(prop, refFn);
		if (obj == null)
			return null;
		Out.put(obj, "flowPropertyType", prop.getFlowPropertyType());
		Out.put(obj, "unitGroup", prop.getUnitGroup(), refFn);
		return obj;
	}

}
