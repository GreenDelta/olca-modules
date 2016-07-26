package org.openlca.jsonld.output;

import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class DQSystemWriter extends Writer<DQSystem> {

	DQSystemWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	JsonObject write(DQSystem system) {
		JsonObject obj = super.write(system);
		if (obj == null)
			return null;
		Out.put(obj, "hasUncertainties", system.hasUncertainties);
		Out.put(obj, "source", system.source, conf);
		writeIndicators(system, obj);
		return obj;
	}

	private void writeIndicators(DQSystem system, JsonObject json) {
		JsonArray indicators = new JsonArray();
		for (DQIndicator i : system.indicators) {
			JsonObject obj = Writer.initJson();
			Out.put(obj, "@type", DQIndicator.class.getSimpleName());
			Out.put(obj, "name", i.name);
			Out.put(obj, "position", i.position);
			writeScores(i, obj);
			indicators.add(obj);
		}
		Out.put(json, "indicators", indicators);
	}

	private void writeScores(DQIndicator indicator, JsonObject json) {
		JsonArray scores = new JsonArray();
		for (DQScore s : indicator.scores) {
			JsonObject obj = Writer.initJson();
			Out.put(obj, "@type", DQScore.class.getSimpleName());
			Out.put(obj, "position", s.position);
			Out.put(obj, "label", s.label);
			Out.put(obj, "description", s.description);
			Out.put(obj, "uncertainty", s.uncertainty);
			scores.add(obj);
		}
		Out.put(json, "scores", scores);
	}

}
