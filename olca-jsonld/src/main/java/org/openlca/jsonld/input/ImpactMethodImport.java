package org.openlca.jsonld.input;

import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.NwSet;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
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
		JsonArray array = Json.getArray(json, "impactCategories");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement e : array) {
			if (!e.isJsonObject())
				continue;
			String catId = Json.getString(e.getAsJsonObject(), "@id");
			ImpactCategory category = ImpactCategoryImport.run(catId, conf);
			if (category != null) {
				m.impactCategories.add(category);
			}
		}
	}

	private void mapNwSets(JsonObject json, ImpactMethod m) {
		JsonArray array = Json.getArray(json, "nwSets");
		if (array == null)
			return;
		for (JsonElement e : array) {
			if (!e.isJsonObject())
				continue;
			String nwSetId = Json.getString(e.getAsJsonObject(), "@id");
			JsonObject nwSetJson = conf.store.get(ModelType.NW_SET, nwSetId);
			NwSet set = NwSetImport.run(m.refId, m.impactCategories, nwSetJson, conf);
			if (set != null)
				m.nwSets.add(set);
		}
	}


}
