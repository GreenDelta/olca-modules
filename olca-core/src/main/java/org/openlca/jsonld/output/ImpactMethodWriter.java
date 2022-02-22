package org.openlca.jsonld.output;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.core.model.ImpactMethod;

class ImpactMethodWriter extends Writer<ImpactMethod> {

	ImpactMethodWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	JsonObject write(ImpactMethod method) {
		var obj = super.write(method);
		if (obj == null)
			return null;
		Out.put(obj, "source", method.source, conf);
		Out.put(obj, "impactCategories", method.impactCategories, conf);
		mapNwSets(obj, method);
		return obj;
	}

	private void mapNwSets(JsonObject obj, ImpactMethod method) {
		if (method.nwSets.isEmpty())
			return;
		var nwSets = new JsonArray();
		for (var nwSet : method.nwSets) {
			var nwObj = new JsonObject();
			Writer.addBasicAttributes(nwSet, nwObj);
			Out.put(nwObj, "weightedScoreUnit", nwSet.weightedScoreUnit);
			var factors = new JsonArray();
			nwSet.factors.stream()
					.map(f -> {
						var factor = new JsonObject();
						Out.put(factor, "@type", "NwFactor");
						Out.put(factor, "impactCategory",f.impactCategory, conf);
						Out.put(factor, "normalisationFactor", f.normalisationFactor);
						Out.put(factor, "weightingFactor", f.weightingFactor);
						return factor;
					})
					.forEach(factors::add);
			nwObj.add("factors", factors);
			nwSets.add(nwObj);
		}
		obj.add("nwSets", nwSets);
	}
}
