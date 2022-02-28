package org.openlca.jsonld.input;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.Json;

class UnitGroupImport extends BaseImport<UnitGroup> {

	private UnitGroupImport(String refId, JsonImport conf) {
		super(ModelType.UNIT_GROUP, refId, conf);
	}

	static UnitGroup run(String refId, JsonImport conf) {
		return new UnitGroupImport(refId, conf).run();
	}

	@Override
	UnitGroup map(JsonObject json, long id) {
		if (json == null)
			return null;
		UnitGroup g = new UnitGroup();
		In.mapAtts(json, g, id, conf);
		addUnits(g, json);
		// insert the unit group before a default flow property is imported
		// to avoid endless import cycles
		g = conf.db.put(g);
		return setDefaultProperty(json, g);
	}

	private UnitGroup setDefaultProperty(JsonObject json, UnitGroup g) {
		String propId = Json.getRefId(json, "defaultFlowProperty");
		if (propId == null)
			return g;
		g.defaultFlowProperty = FlowPropertyImport.run(propId, conf);
		return conf.db.update(g);
	}

	private void addUnits(UnitGroup group, JsonObject json) {
		JsonArray array = Json.getArray(json, "units");
		if (array == null || array.size() == 0)
			return;

		// sync. with existing units if we are in
		// mode to keep existing IDs as these units
		// may are used in other objects
		Map<String, Unit> oldUnits = null;
		UnitGroup oldGroup = conf.db.get(
				ModelType.UNIT_GROUP, group.refId);
		if (oldGroup != null) {
			oldUnits = new HashMap<>();
			for (var oldUnit : oldGroup.units) {
				oldUnits.put(oldUnit.name, oldUnit);
			}
		}

		for (JsonElement e : array) {
			if (!e.isJsonObject())
				continue;
			var unitJson = e.getAsJsonObject();
			var name = Json.getString(unitJson, "name");
			if (name == null)
				continue;

			// get an old unit to update it or
			// create a new one
			Unit unit = null;
			if (oldUnits != null) {
				unit = oldUnits.get(name);
			}
			if (unit == null) {
				unit = new Unit();
			}

			// map unit attributes
			In.mapAtts(unitJson, unit, unit.id);
			unit.conversionFactor = Json.getDouble(
					unitJson, "conversionFactor", 1.0);
			var synonyms = Json.getArray(unitJson, "synonyms");
			if (synonyms != null) {
				unit.synonyms = Json.stream(synonyms)
						.filter(elem -> e.isJsonPrimitive())
						.map((elem -> e.getAsString()))
						.reduce((acc, syn) -> acc + ";" + syn)
						.orElse(null);
			}

			boolean refUnit = Json.getBool(unitJson, "isReferenceUnit", false);
			if (refUnit) {
				group.referenceUnit = unit;
			}
			group.units.add(unit);
		}
	}

}
