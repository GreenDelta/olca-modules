package org.openlca.jsonld.input;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.Json;

import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class UnitImport extends BaseEmbeddedImport<Unit, UnitGroup> {

	private UnitImport(String unitGroupRefId, ImportConfig conf) {
		super(ModelType.UNIT_GROUP, unitGroupRefId, conf);
	}

	static Unit run(String unitGroupRefId, JsonObject json, ImportConfig conf) {
		return new UnitImport(unitGroupRefId, conf).run(json);
	}

	@Override
	Unit map(JsonObject json, long id) {
		Unit u = new Unit();
		In.mapAtts(json, u, id);
		u.conversionFactor = Json.getDouble(json, "conversionFactor", 1.0);
		addSynonyms(u, json);
		return u;
	}

	private void addSynonyms(Unit unit, JsonObject json) {
		JsonArray array = Json.getArray(json, "synonyms");
		if (array == null || array.size() == 0)
			return;
		List<String> synonyms = new ArrayList<>();
		for (JsonElement e : array) {
			if (!e.isJsonPrimitive())
				continue;
			synonyms.add(e.getAsString());
		}
		String synStr = Joiner.on(';').join(synonyms);
		unit.synonyms = synStr;
	}

	@Override
	Unit getPersisted(UnitGroup unitGroup, JsonObject json) {
		String name = Json.getString(json, "name");
		if (name == null)
			return null;
		return unitGroup.getUnit(name);
	}
	
}
