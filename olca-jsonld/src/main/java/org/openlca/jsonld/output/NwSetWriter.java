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
		obj.addProperty("weightedScoreUnit", set.getWeightedScoreUnit());
		JsonArray factors = new JsonArray();
		for (NwFactor f : set.getFactors()) {
			JsonObject fObj = new JsonObject();
			JsonObject cat = References.create(f.getImpactCategory(), refFn);
			fObj.add("impactCategory", cat);
			fObj.addProperty("normalisationFactor", f.getNormalisationFactor());
			fObj.addProperty("weightingFactor", f.getWeightingFactor());
			factors.add(fObj);
		}
		obj.add("factors", factors);
		return obj;
	}

}
