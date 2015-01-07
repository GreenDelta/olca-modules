package org.openlca.jsonld;

import java.lang.reflect.Type;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

class UnitGroupWriter implements Writer<UnitGroup> {

	private EntityStore store;

	public UnitGroupWriter() {
	}

	public UnitGroupWriter(EntityStore store) {
		this.store = store;
	}

	@Override
	public void write(UnitGroup group) {
		if (group == null || store == null)
			return;
		if (store.contains(ModelType.UNIT_GROUP, group.getRefId()))
			return;
		JsonObject obj = serialize(group, null, null);
		store.add(ModelType.UNIT_GROUP, group.getRefId(), obj);
	}

	@Override
	public JsonObject serialize(UnitGroup unitGroup, Type type,
			JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		JsonWriter.addContext(obj);
		map(unitGroup, obj);
		return obj;
	}

	private void map(UnitGroup group, JsonObject obj) {
		if (group == null || obj == null)
			return;
		JsonWriter.addAttributes(group, obj, store);
		JsonObject propRef = Refs.createRef(group.getDefaultFlowProperty());
		obj.add("defaultFlowProperty", propRef);
		JsonObject unitRef = Refs.createRef(group.getReferenceUnit());
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
