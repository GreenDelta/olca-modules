package org.openlca.jsonld.input;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class UnitGroupImport extends BaseImport<UnitGroup> {

	private UnitGroupImport(String refId, ImportConfig conf) {
		super(ModelType.UNIT_GROUP, refId, conf);
	}

	static UnitGroup run(String refId, ImportConfig conf) {
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
		FlowProperty prop = FlowPropertyImport.run(propId, conf);
		g.defaultFlowProperty = prop;
		return conf.db.update(g);
	}

	private void addUnits(UnitGroup g, JsonObject json) {
		JsonArray array = Json.getArray(json, "units");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement e : array) {
			if (!e.isJsonObject())
				continue;
			JsonObject obj = e.getAsJsonObject();
			Unit unit = UnitImport.run(g.refId, obj, conf);
			boolean refUnit = Json.getBool(obj, "referenceUnit", false);
			if (refUnit)
				g.referenceUnit = unit;
			g.units.add(unit);
		}
	}

}
