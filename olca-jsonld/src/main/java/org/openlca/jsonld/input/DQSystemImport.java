package org.openlca.jsonld.input;

import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.ModelType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class DQSystemImport extends BaseImport<DQSystem> {

	private DQSystemImport(String refId, ImportConfig conf) {
		super(ModelType.DQ_SYSTEM, refId, conf);
	}

	static DQSystem run(String refId, ImportConfig conf) {
		return new DQSystemImport(refId, conf).run();
	}

	@Override
	DQSystem map(JsonObject json, long id) {
		if (json == null)
			return null;
		DQSystem s = new DQSystem();
		In.mapAtts(json, s, id, conf);
		s.hasUncertainties = In.getBool(json, "hasUncertainties", false);
		String sourceRefId = In.getRefId(json, "source");
		if (sourceRefId != null)
			s.source = SourceImport.run(sourceRefId, conf);
		mapIndicators(json, s);
		return conf.db.put(s);
	}

	private void mapIndicators(JsonObject json, DQSystem s) {
		JsonArray indicators = In.getArray(json, "indicators");
		if (indicators == null || indicators.size() == 0)
			return;
		for (JsonElement e : indicators) {
			if (!e.isJsonObject())
				continue;
			JsonObject i = e.getAsJsonObject();
			DQIndicator indicator = new DQIndicator();
			indicator.name = In.getString(i, "name");
			indicator.position = In.getInt(i, "position", 0);
			mapScores(i, indicator);
			s.indicators.add(indicator);
		}
	}

	private void mapScores(JsonObject json, DQIndicator i) {
		JsonArray scores = In.getArray(json, "scores");
		if (scores == null || scores.size() == 0)
			return;
		for (JsonElement e : scores) {
			if (!e.isJsonObject())
				continue;
			JsonObject s = e.getAsJsonObject();
			DQScore score = new DQScore();
			score.position = In.getInt(s, "position", 0);
			score.label = In.getString(s, "label");
			score.description = In.getString(s, "description");
			score.uncertainty = In.getDouble(s, "uncertainty", 0);
			i.scores.add(score);
		}
	}

}
