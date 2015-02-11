package org.openlca.jsonld.output;

import java.lang.reflect.Type;
import java.util.Objects;

import com.google.gson.JsonPrimitive;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.EntityStore;

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
		store.put(ModelType.UNIT_GROUP, obj);
	}

	@Override
	public JsonObject serialize(UnitGroup unitGroup, Type type,
			JsonSerializationContext context) {
		JsonObject obj = store == null ? new JsonObject() : store.initJson();
		map(unitGroup, obj);
		return obj;
	}

	private void map(UnitGroup group, JsonObject obj) {
		if (group == null || obj == null)
			return;
		JsonExport.addAttributes(group, obj, store);
		JsonObject propRef = Out.createRef(group.getDefaultFlowProperty());
		obj.add("defaultFlowProperty", propRef);
		addUnits(group, obj);
	}

	private void addUnits(UnitGroup group, JsonObject obj) {
		if(group == null || obj == null)
			return;
		JsonArray units = new JsonArray();
		for (Unit unit : group.getUnits()) {
			JsonObject unitObj = new JsonObject();
			JsonExport.addAttributes(unit, unitObj, null);
			if(Objects.equals(unit, group.getReferenceUnit()))
				unitObj.add("referenceUnit", new JsonPrimitive(true));
			unitObj.addProperty("conversionFactor", unit.getConversionFactor());
			addSynonyms(unit, unitObj);
			units.add(unitObj);
		}
		obj.add("units", units);
	}

	private void addSynonyms(Unit unit, JsonObject object) {
		String synonyms = unit.getSynonyms();
		if (synonyms == null || synonyms.trim().isEmpty())
			return;
		JsonArray array = new JsonArray();
		String[] items = synonyms.split(";");
		for (String item : items)
			array.add(new JsonPrimitive(item.trim()));
		object.add("synonyms", array);
	}
}
