package org.openlca.jsonld.output;

import java.util.Objects;

import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class UnitGroupWriter extends Writer<UnitGroup> {

	UnitGroupWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	JsonObject write(UnitGroup ug) {
		JsonObject obj = super.write(ug);
		if (obj == null)
			return null;
		mapUnits(ug, obj);
		Out.put(obj, "defaultFlowProperty", ug.getDefaultFlowProperty(), conf);
		return obj;
	}

	private void mapUnits(UnitGroup group, JsonObject json) {
		JsonArray units = new JsonArray();
		for (Unit unit : group.getUnits()) {
			JsonObject obj = new JsonObject();
			addBasicAttributes(unit, obj);
			if (Objects.equals(unit, group.getReferenceUnit()))
				Out.put(obj, "referenceUnit", true);
			Out.put(obj, "conversionFactor", unit.getConversionFactor());
			mapSynonyms(unit, obj);
			units.add(obj);
		}
		Out.put(json, "units", units);
	}

	private void mapSynonyms(Unit unit, JsonObject obj) {
		String synonyms = unit.getSynonyms();
		if (synonyms == null || synonyms.trim().isEmpty())
			return;
		JsonArray array = new JsonArray();
		String[] items = synonyms.split(";");
		for (String item : items)
			array.add(new JsonPrimitive(item.trim()));
		Out.put(obj, "synonyms", array);
	}
}
