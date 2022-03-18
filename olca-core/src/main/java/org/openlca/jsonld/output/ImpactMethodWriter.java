package org.openlca.jsonld.output;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.core.model.ImpactMethod;
import org.openlca.jsonld.Json;

record ImpactMethodWriter(JsonExport exp) implements Writer<ImpactMethod> {

	@Override
	public JsonObject write(ImpactMethod method) {
		var obj = Writer.init(method);
		Json.put(obj, "code", method.code);
		Json.put(obj, "source", exp.handleRef(method.source));
		Json.put(obj, "impactCategories", exp.handleRefs(method.impactCategories));
		mapNwSets(obj, method);
		return obj;
	}

	private void mapNwSets(JsonObject obj, ImpactMethod method) {
		if (method.nwSets.isEmpty())
			return;
		var nwSets = new JsonArray();
		for (var nwSet : method.nwSets) {
			var nwObj = new JsonObject();
			Writer.mapBasicAttributes(nwSet, nwObj);
			Json.put(nwObj, "weightedScoreUnit", nwSet.weightedScoreUnit);
			var factors = new JsonArray();
			nwSet.factors.stream()
				.map(f -> {
					var factor = new JsonObject();
					Json.put(factor, "impactCategory", exp.handleRef(f.impactCategory));
					Json.put(factor, "normalisationFactor", f.normalisationFactor);
					Json.put(factor, "weightingFactor", f.weightingFactor);
					return factor;
				})
				.forEach(factors::add);
			nwObj.add("factors", factors);
			nwSets.add(nwObj);
		}
		obj.add("nwSets", nwSets);
	}
}
