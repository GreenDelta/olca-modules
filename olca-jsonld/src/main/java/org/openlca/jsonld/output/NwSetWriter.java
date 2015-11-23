package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.RootEntity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class NwSetWriter extends Writer<NwSet> {

	@Override
	JsonObject write(NwSet set, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(set, refFn);
		if (obj == null)
			return null;
		Out.put(obj, "weightedScoreUnit", set.getWeightedScoreUnit());
		mapFactors(set, obj, refFn);
		return obj;
	}

	private void mapFactors(NwSet set, JsonObject json,
			Consumer<RootEntity> refFn) {
		JsonArray factors = new JsonArray();
		for (NwFactor f : set.getFactors()) {
			JsonObject obj = new JsonObject();
			Out.put(obj, "impactCategory", f.getImpactCategory(), refFn);
			Out.put(obj, "normalisationFactor", f.getNormalisationFactor());
			Out.put(obj, "weightingFactor", f.getWeightingFactor());
			factors.add(obj);
		}
		Out.put(json, "factors", factors);
	}

}
