package org.openlca.jsonld;

import java.lang.reflect.Type;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

class UnitGroupWriter implements Writer<UnitGroup> {

	@Override
	public void write(UnitGroup group, EntityStore store) {
		if(group == null || store == null)
			return;
		JsonObject obj = serialize(group, null, null);
		store.add(ModelType.UNIT_GROUP, obj);
	}

	@Override
	public JsonObject serialize(UnitGroup unitGroup, Type type,
			JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		JsonWriter.addContext(obj);
		map(unitGroup, obj);
		return obj;
	}

	static void map(UnitGroup group, JsonObject obj) {
		if (group == null || obj == null)
			return;
		JsonWriter.addAttributes(group, obj);
		JsonObject propRef = JsonWriter.createRef(group
				.getDefaultFlowProperty());
		obj.add("defaultFlowProperty", propRef);
		JsonObject unitRef = JsonWriter.createRef(group
				.getReferenceUnit());
		obj.add("referenceUnit", unitRef);
		JsonArray units = new JsonArray();
		for (Unit unit : group.getUnits()) {
			JsonObject unitObj = new JsonObject();
			UnitWriter.map(unit, unitObj);
			units.add(unitObj);
		}
		obj.add("units", units);
	}

}
