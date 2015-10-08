package org.openlca.jsonld.output;

import java.util.Objects;
import java.util.function.Consumer;

import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class UnitGroupWriter extends Writer<UnitGroup> {

	@Override
	JsonObject write(UnitGroup group, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(group, refFn);
		if (obj == null)
			return null;
		JsonObject propRef = createRef(group.getDefaultFlowProperty());
		obj.add("defaultFlowProperty", propRef);
		addUnits(group, obj);
		return obj;
	}

	private void addUnits(UnitGroup group, JsonObject obj) {
		if (group == null || obj == null)
			return;
		JsonArray units = new JsonArray();
		for (Unit unit : group.getUnits()) {
			JsonObject unitObj = new JsonObject();
			addBasicAttributes(unit, unitObj);
			if (Objects.equals(unit, group.getReferenceUnit()))
				unitObj.addProperty("referenceUnit", true);
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
