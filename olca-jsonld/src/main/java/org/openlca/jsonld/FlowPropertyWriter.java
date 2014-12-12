package org.openlca.jsonld;

import java.lang.reflect.Type;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.ModelType;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

class FlowPropertyWriter implements Writer<FlowProperty> {

	private EntityStore store;
	private boolean writeContext = true;

	public FlowPropertyWriter() {
	}

	public FlowPropertyWriter(EntityStore store) {
		this.store = store;
	}

	@Override
	public void skipContext() {
		this.writeContext = false;
	}

	@Override
	public void write(FlowProperty property) {
		if (property == null || store == null)
			return;
		if (store.contains(ModelType.FLOW_PROPERTY, property.getRefId()))
			return;
		JsonObject obj = serialize(property, null, null);
		store.add(ModelType.FLOW_PROPERTY, property.getRefId(), obj);
	}

	@Override
	public JsonObject serialize(FlowProperty property, Type type,
			JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		if (writeContext)
			JsonWriter.addContext(obj);
		map(property, obj);
		return obj;
	}

	private void map(FlowProperty property, JsonObject obj) {
		if (property == null || obj == null)
			return;
		JsonWriter.addAttributes(property, obj);
		mapType(property, obj);
		JsonObject unitGroup = Refs.put(property.getUnitGroup(), store);
		obj.add("unitGroup", unitGroup);
	}

	private static void mapType(FlowProperty property, JsonObject obj) {
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
