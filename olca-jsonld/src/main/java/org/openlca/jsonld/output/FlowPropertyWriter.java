package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.RootEntity;

import com.google.gson.JsonObject;

class FlowPropertyWriter extends Writer<FlowProperty> {

	@Override
	public JsonObject write(FlowProperty prop, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(prop, refFn);
		if (obj == null)
			return null;
		mapType(prop, obj);
		JsonObject unitGroup = References.create(prop.getUnitGroup(), refFn);
		obj.add("unitGroup", unitGroup);
		return obj;
	}

	private void mapType(FlowProperty property, JsonObject obj) {
		FlowPropertyType type = property.getFlowPropertyType();
		if (type == null)
			return;
		switch (type) {
		case ECONOMIC:
			obj.addProperty("flowPropertyType", "ECONOMIC_QUANTITY");
			break;
		case PHYSICAL:
			obj.addProperty("flowPropertyType", "PHYSICAL_QUANTITY");
			break;
		}
	}

}
