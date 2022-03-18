package org.openlca.jsonld.output;

import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

record DQSystemWriter(JsonExport exp) implements Writer<DQSystem> {

	@Override
	public JsonObject write(DQSystem system) {
		var obj = Writer.init(system);
		Json.put(obj, "hasUncertainties", system.hasUncertainties);
		Json.put(obj, "source", exp.handleRef(system.source));
		writeIndicators(system, obj);
		return obj;
	}

	private void writeIndicators(DQSystem system, JsonObject json) {
		var indicators = new JsonArray();
		for (DQIndicator i : system.indicators) {
			var obj = new JsonObject();
			Json.put(obj, "name", i.name);
			Json.put(obj, "position", i.position);
			writeScores(i, obj);
			indicators.add(obj);
		}
		Json.put(json, "indicators", indicators);
	}

	private void writeScores(DQIndicator indicator, JsonObject json) {
		JsonArray scores = new JsonArray();
		for (DQScore s : indicator.scores) {
			var obj = new JsonObject();
			Json.put(obj, "position", s.position);
			Json.put(obj, "label", s.label);
			Json.put(obj, "description", s.description);
			Json.put(obj, "uncertainty", s.uncertainty);
			scores.add(obj);
		}
		Json.put(json, "scores", scores);
	}

}
