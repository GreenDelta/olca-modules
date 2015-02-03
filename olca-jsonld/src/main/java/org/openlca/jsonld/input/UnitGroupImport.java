package org.openlca.jsonld.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.EntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UnitGroupImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private String refId;
	private EntityStore store;
	private Db db;

	private UnitGroupImport(String refId, EntityStore store, Db db) {
		this.refId = refId;
		this.store = store;
		this.db = db;
	}

	static UnitGroup run(String refId, EntityStore store, Db db) {
		return new UnitGroupImport(refId, store, db).run();
	}

	private UnitGroup run() {
		if (refId == null || store == null || db == null)
			return null;
		try {
			UnitGroup g = db.getUnitGroup(refId);
			if (g != null)
				return g;
			JsonObject json = store.get(ModelType.UNIT_GROUP, refId);
			return map(json);
		} catch (Exception e) {
			log.error("failed to import unit group " + refId, e);
			return null;
		}
	}

	private UnitGroup map(JsonObject json) {
		if (json == null)
			return null;
		UnitGroup g = new UnitGroup();
		In.mapAtts(json, g);
		String catId = In.getRefId(json, "category");
		g.setCategory(CategoryImport.run(catId, store, db));
		addUnits(g, json);
		setRefUnit(g, json);
		// insert the unit group before a default flow property is imported
		// to avoid endless import cycles
		g = db.put(g);
		g = setDefaultProperty(json, g);
		return g;
	}

	private UnitGroup setDefaultProperty(JsonObject json, UnitGroup g) {
		String propId = In.getRefId(json, "defaultFlowProperty");
		if (propId == null)
			return g;
		FlowProperty prop = FlowPropertyImport.run(propId, store, db);
		g.setDefaultFlowProperty(prop);
		return db.update(g);
	}

	private void addUnits(UnitGroup g, JsonObject json) {
		JsonElement elem = json.get("units");
		if (elem == null || !elem.isJsonArray())
			return;
		for (JsonElement e : elem.getAsJsonArray()) {
			if (!e.isJsonObject())
				continue;
			JsonObject obj = e.getAsJsonObject();
			Unit unit = mapUnit(obj);
			g.getUnits().add(unit);
		}
	}

	private Unit mapUnit(JsonObject json) {
		Unit unit = new Unit();
		In.mapAtts(json, unit);
		unit.setConversionFactor(In.getDouble(json, "conversionFactor", 1.0));
		addSynonyms(unit, json);
		return unit;
	}

	private void addSynonyms(Unit unit, JsonObject json) {
		JsonElement elem = json.get("synonyms");
		if (elem == null || !elem.isJsonArray())
			return;
		List<String> synonyms = new ArrayList<>();
		for (JsonElement e : elem.getAsJsonArray()) {
			if (!e.isJsonPrimitive())
				continue;
			synonyms.add(e.getAsString());
		}
		String synStr = Joiner.on(';').join(synonyms);
		unit.setSynonyms(synStr);
	}

	private void setRefUnit(UnitGroup g, JsonObject json) {
		String refId = In.getRefId(json, "referenceUnit");
		for (Unit u : g.getUnits()) {
			if (Objects.equals(refId, u.getRefId())) {
				g.setReferenceUnit(u);
				break;
			}
		}
	}
}
