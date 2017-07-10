package org.openlca.jsonld.output;

import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class NwSetWriter extends Writer<NwSet> {

	NwSetWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	JsonObject write(NwSet set) {
		JsonObject obj = super.write(set);
		if (obj == null)
			return null;
		Out.put(obj, "weightedScoreUnit", set.weightedScoreUnit);
		mapFactors(set, obj);
		return obj;
	}

	private void mapFactors(NwSet set, JsonObject json) {
		JsonArray factors = new JsonArray();
		for (NwFactor f : set.factors) {
			JsonObject obj = new JsonObject();
			Out.put(obj, "@type", NwFactor.class.getSimpleName());
			Out.put(obj, "impactCategory", f.getImpactCategory(), conf, Out.REQUIRED_FIELD);
			Out.put(obj, "normalisationFactor", f.getNormalisationFactor());
			Out.put(obj, "weightingFactor", f.getWeightingFactor());
			factors.add(obj);
		}
		Out.put(json, "factors", factors);
	}

}
