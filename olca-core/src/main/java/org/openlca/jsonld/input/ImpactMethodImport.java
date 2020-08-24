package org.openlca.jsonld.input;

import java.util.Objects;

import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;
import org.openlca.jsonld.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ImpactMethodImport extends BaseImport<ImpactMethod> {

	private ImpactMethodImport(String refId, ImportConfig conf) {
		super(ModelType.IMPACT_METHOD, refId, conf);
	}

	static ImpactMethod run(String refId, ImportConfig conf) {
		return new ImpactMethodImport(refId, conf).run();
	}

	@Override
	ImpactMethod map(JsonObject json, long id) {
		if (json == null)
			return null;
		ImpactMethod m = new ImpactMethod();
		In.mapAtts(json, m, id, conf);
		// first map categories, nw sets will reference them
		mapCategories(json, m);
		mapNwSets(json, m);
		return conf.db.put(m);
	}

	private void mapCategories(JsonObject json, ImpactMethod m) {
		var array = Json.getArray(json, "impactCategories");
		if (array == null || array.size() == 0)
			return;
		for (var e : array) {
			if (!e.isJsonObject())
				continue;
			var catId = Json.getString(e.getAsJsonObject(), "@id");
			var impact = ImpactCategoryImport.run(catId, conf);
			if (impact != null) {
				m.impactCategories.add(impact);
			}
		}
	}

	private void mapNwSets(JsonObject json, ImpactMethod method) {
		var array = Json.getArray(json, "nwSets");
		if (array == null)
			return;
		for (JsonElement e : array) {
			if (!e.isJsonObject())
				continue;
			var nwObj = e.getAsJsonObject();
			var nwSet = new NwSet();
			method.nwSets.add(nwSet);
			In.mapAtts(nwObj, nwSet, 0L);
			nwSet.weightedScoreUnit = Json.getString(
					json, "weightedScoreUnit");
			Json.stream(Json.getArray(nwObj, "factors"))
					.filter(JsonElement::isJsonObject)
					.map(f -> nwFactor(f.getAsJsonObject(), method))
					.forEach(nwSet.factors::add);
		}
	}

	private NwFactor nwFactor(JsonObject json, ImpactMethod method) {
		var f = new NwFactor();
		var impactID = Json.getRefId(json, "impactCategory");
		f.impactCategory = method.impactCategories.stream()
				.filter(i -> Objects.equals(i.refId, impactID))
				.findAny()
				.orElse(null);
		f.normalisationFactor = Json.getDouble(json, "normalisationFactor")
				.orElse(null);
		f.weightingFactor = Json.getDouble(json, "weightingFactor")
				.orElse(null);
		return f;
	}
}
