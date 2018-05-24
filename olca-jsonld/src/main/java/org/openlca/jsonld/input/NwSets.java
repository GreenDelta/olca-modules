package org.openlca.jsonld.input;

import java.util.List;

import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class NwSets {

	static NwSet map(JsonObject json, List<ImpactCategory> categories) {
		if (json == null)
			return null;
		NwSet set = new NwSet();
		In.mapAtts(json, set, 0);
		set.weightedScoreUnit = Json.getString(json, "weightedScoreUnit");
		JsonArray factors = Json.getArray(json, "factors");
		if (factors == null)
			return set;
		for (JsonElement f : factors) {
			if (!f.isJsonObject())
				continue;
			NwFactor factor = mapFactor(f.getAsJsonObject(), categories);
			set.factors.add(factor);
		}
		return set;
	}

	private static NwFactor mapFactor(JsonObject json,
			List<ImpactCategory> categories) {
		NwFactor f = new NwFactor();
		String categoryId = Json.getRefId(json, "impactCategory");
		f.setImpactCategory(getImpactCategory(categoryId, categories));
		f.setNormalisationFactor(Json.getOptionalDouble(json, "normalisationFactor"));
		f.setWeightingFactor(Json.getOptionalDouble(json, "weightingFactor"));
		return f;
	}

	private static ImpactCategory getImpactCategory(String refId,
			List<ImpactCategory> categories) {
		for (ImpactCategory category : categories)
			if (category.getRefId().equals(refId))
				return category;
		return null;
	}

}
