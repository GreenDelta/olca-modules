package org.openlca.jsonld.input;

import java.util.Objects;

import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Source;
import org.openlca.jsonld.Json;

public record DQSystemReader(EntityResolver resolver)
	implements EntityReader<DQSystem> {

	public DQSystemReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public DQSystem read(JsonObject json) {
		var dqSystem = new DQSystem();
		update(dqSystem, json);
		return dqSystem;
	}

	@Override
	public void update(DQSystem dqSystem, JsonObject json) {
		Util.mapBase(dqSystem, json, resolver);
		dqSystem.hasUncertainties = Json.getBool(json, "hasUncertainties", false);
		var sourceRefId = Json.getRefId(json, "source");
		if (sourceRefId != null) {
			dqSystem.source = resolver.get(Source.class, sourceRefId);
		}
		mapIndicators(dqSystem, json);
	}

	private void mapIndicators(DQSystem dqSystem, JsonObject json) {
		dqSystem.indicators.clear();
		var indicators = Json.getArray(json, "indicators");
		if (indicators == null || indicators.size() == 0)
			return;
		for (var e : indicators) {
			if (!e.isJsonObject())
				continue;
			var indicatorJson = e.getAsJsonObject();
			var indicator = new DQIndicator();
			indicator.name = Json.getString(indicatorJson, "name");
			indicator.position = Json.getInt(indicatorJson, "position", 0);
			mapScores(indicator, indicatorJson);
			dqSystem.indicators.add(indicator);
		}
	}

	private void mapScores(DQIndicator indicator, JsonObject indicatorJson) {
		var array = Json.getArray(indicatorJson, "scores");
		if (array == null || array.size() == 0)
			return;
		for (var e : array) {
			if (!e.isJsonObject())
				continue;
			var scoreJson = e.getAsJsonObject();
			var score = new DQScore();
			score.position = Json.getInt(scoreJson, "position", 0);
			score.label = Json.getString(scoreJson, "label");
			score.description = Json.getString(scoreJson, "description");
			score.uncertainty = Json.getDouble(scoreJson, "uncertainty", 0);
			indicator.scores.add(score);
		}
	}
}
