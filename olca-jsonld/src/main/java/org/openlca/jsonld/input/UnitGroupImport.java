package org.openlca.jsonld.input;

import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UnitGroupImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private String refId;
	private ImportConfig conf;

	private UnitGroupImport(String refId, ImportConfig conf) {
		this.refId = refId;
		this.conf = conf;
	}

	static UnitGroup run(String refId, ImportConfig conf) {
		return new UnitGroupImport(refId, conf).run();
	}

	private UnitGroup run() {
		if (refId == null || conf == null)
			return null;
		try {
			UnitGroup g = conf.db.getUnitGroup(refId);
			if (g != null)
				return g;
			JsonObject json = conf.store.get(ModelType.UNIT_GROUP, refId);
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
		g.setCategory(CategoryImport.run(catId, conf));
		addUnits(g, json);
		// insert the unit group before a default flow property is imported
		// to avoid endless import cycles
		g = conf.db.put(g);
		g = setDefaultProperty(json, g);
		return g;
	}

	private UnitGroup setDefaultProperty(JsonObject json, UnitGroup g) {
		String propId = In.getRefId(json, "defaultFlowProperty");
		if (propId == null)
			return g;
		FlowProperty prop = FlowPropertyImport.run(propId, conf);
		g.setDefaultFlowProperty(prop);
		return conf.db.update(g);
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
			boolean refUnit = In.getBool(obj, "referenceUnit", false);
			if(refUnit)
				g.setReferenceUnit(unit);
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
}
