package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.Enums;

import com.google.gson.JsonObject;

class FlowPropertyWriter extends Writer<FlowProperty> {

	@Override
	public JsonObject write(FlowProperty prop, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(prop, refFn);
		if (obj == null)
			return null;
		obj.addProperty("flowPropertyType", Enums.getLabel(
				prop.getFlowPropertyType(), FlowPropertyType.class));
		JsonObject unitGroup = References.create(prop.getUnitGroup(), refFn);
		obj.add("unitGroup", unitGroup);
		return obj;
	}

}
